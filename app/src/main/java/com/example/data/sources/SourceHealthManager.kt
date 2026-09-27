package com.example.data.sources

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.ConcurrentHashMap

enum class SourceHealthStatus(val emoji: String, val label: String) {
    HEALTHY("🟢", "Healthy"),
    INTERMITTENT("🟡", "Intermittent"),
    BROKEN("🔴", "Broken"),
    DISABLED("⚪", "Disabled")
}

enum class FailureReason(val description: String) {
    NONE("No errors"),
    CLOUDFLARE_403("Cloudflare challenge or 403 Forbidden"),
    NOT_FOUND_404("Source endpoint 404 / site layout changed"),
    RATE_LIMITED_429("Rate limited (HTTP 429) - backing off"),
    TIMEOUT("Request timed out"),
    PARSER_ERROR("HTML/JSON parsing failure"),
    NETWORK_ERROR("Network connection error")
}

data class SourceHealthRecord(
    val sourceId: String,
    val status: SourceHealthStatus = SourceHealthStatus.HEALTHY,
    val failureCount: Int = 0,
    val lastSuccessTime: Long = System.currentTimeMillis(),
    val lastFailureTime: Long = 0L,
    val lastError: String? = null,
    val failureReason: FailureReason = FailureReason.NONE,
    val circuitBreakerUntil: Long = 0L,
    val responseTimeMs: Long = 0L,
    val needsWebViewChallenge: Boolean = false
)

data class DiagnosticResult(
    val sourceId: String,
    val isSuccess: Boolean,
    val pingMs: Long,
    val popularCount: Int,
    val failureReason: FailureReason,
    val message: String
)

/**
 * SourceHealthManager
 * Monitors source reliability, classifies failure types, implements circuit-breakers,
 * triggers 1-retry with backoff, and provides diagnostic tests.
 */
object SourceHealthManager {

    private val healthRecords = ConcurrentHashMap<String, SourceHealthRecord>()
    private val _healthFlow = MutableStateFlow<Map<String, SourceHealthRecord>>(emptyMap())
    val healthFlow: StateFlow<Map<String, SourceHealthRecord>> = _healthFlow.asStateFlow()

    fun getHealth(sourceId: String): SourceHealthRecord {
        return healthRecords.getOrPut(sourceId) {
            SourceHealthRecord(sourceId = sourceId, status = SourceHealthStatus.HEALTHY)
        }
    }

    /**
     * Executes a source request with timeout, 1-retry, failure classification,
     * circuit-breaker checks, and health updates.
     */
    suspend fun <T> executeWithHealth(
        sourceId: String,
        timeoutMs: Long = 12000L,
        block: suspend () -> Result<T>
    ): Result<T> = withContext(Dispatchers.IO) {
        val currentRecord = getHealth(sourceId)
        val now = System.currentTimeMillis()

        // Check Circuit Breaker
        if (currentRecord.circuitBreakerUntil > now) {
            val remainingSec = (currentRecord.circuitBreakerUntil - now) / 1000
            return@withContext Result.failure(
                IOException("Source '$sourceId' circuit breaker open (${currentRecord.failureReason.description}). Cooling down for ${remainingSec}s")
            )
        }

        val startTime = System.currentTimeMillis()
        var lastThrownException: Throwable? = null

        // Try request with 1 retry (max 2 attempts total)
        for (attempt in 1..2) {
            try {
                val result = withTimeout(timeoutMs) {
                    block()
                }

                if (result.isSuccess) {
                    val duration = System.currentTimeMillis() - startTime
                    recordSuccess(sourceId, duration)
                    return@withContext result
                } else {
                    lastThrownException = result.exceptionOrNull()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Throwable) {
                lastThrownException = e
            }

            // Exponential backoff before retry attempt
            if (attempt == 1) {
                delay(600)
            }
        }

        // Both attempts failed: classify failure
        val failureReason = classifyFailure(lastThrownException)
        recordFailure(sourceId, failureReason, lastThrownException?.message)

        Result.failure(
            lastThrownException ?: IOException("Request to $sourceId failed ($failureReason)")
        )
    }

    private fun classifyFailure(throwable: Throwable?): FailureReason {
        if (throwable == null) return FailureReason.NONE
        val msg = throwable.message?.lowercase() ?: ""

        return when {
            msg.contains("cloudflare") || msg.contains("403") || msg.contains("cf-mitigated") || msg.contains("turnstile") ->
                FailureReason.CLOUDFLARE_403
            msg.contains("404") || msg.contains("not found") ->
                FailureReason.NOT_FOUND_404
            msg.contains("429") || msg.contains("too many requests") ->
                FailureReason.RATE_LIMITED_429
            throwable is SocketTimeoutException || msg.contains("timeout") || msg.contains("timed out") ->
                FailureReason.TIMEOUT
            msg.contains("json") || msg.contains("parse") || msg.contains("selector") || msg.contains("nullpointer") ->
                FailureReason.PARSER_ERROR
            else -> FailureReason.NETWORK_ERROR
        }
    }

    private fun recordSuccess(sourceId: String, responseTimeMs: Long) {
        val current = getHealth(sourceId)
        val updated = current.copy(
            status = SourceHealthStatus.HEALTHY,
            failureCount = 0,
            lastSuccessTime = System.currentTimeMillis(),
            lastError = null,
            failureReason = FailureReason.NONE,
            circuitBreakerUntil = 0L,
            responseTimeMs = responseTimeMs,
            needsWebViewChallenge = false
        )
        healthRecords[sourceId] = updated
        _healthFlow.value = healthRecords.toMap()
    }

    private fun recordFailure(sourceId: String, reason: FailureReason, errorMsg: String?) {
        val current = getHealth(sourceId)
        val newFailureCount = current.failureCount + 1
        val now = System.currentTimeMillis()

        val newStatus = when {
            newFailureCount == 1 -> SourceHealthStatus.INTERMITTENT
            else -> SourceHealthStatus.BROKEN
        }

        // Trip circuit-breaker if 3 or more consecutive failures
        val circuitBreakerDuration = if (newFailureCount >= 3) {
            now + 60_000L // 60 seconds cool-down
        } else {
            0L
        }

        val updated = current.copy(
            status = newStatus,
            failureCount = newFailureCount,
            lastFailureTime = now,
            lastError = errorMsg,
            failureReason = reason,
            circuitBreakerUntil = circuitBreakerDuration,
            needsWebViewChallenge = (reason == FailureReason.CLOUDFLARE_403)
        )
        healthRecords[sourceId] = updated
        _healthFlow.value = healthRecords.toMap()
    }

    /**
     * Diagnostic test for a given source:
     * Pings the source, checks response time, and provides status report.
     */
    suspend fun runDiagnostics(
        sourceId: String,
        testCall: suspend () -> Result<Int>
    ): DiagnosticResult = withContext(Dispatchers.IO) {
        val start = System.currentTimeMillis()
        try {
            val res = testCall()
            val ping = System.currentTimeMillis() - start
            if (res.isSuccess) {
                val count = res.getOrDefault(0)
                recordSuccess(sourceId, ping)
                DiagnosticResult(
                    sourceId = sourceId,
                    isSuccess = true,
                    pingMs = ping,
                    popularCount = count,
                    failureReason = FailureReason.NONE,
                    message = "Healthy (Response: ${ping}ms, $count manga returned)"
                )
            } else {
                val reason = classifyFailure(res.exceptionOrNull())
                recordFailure(sourceId, reason, res.exceptionOrNull()?.message)
                DiagnosticResult(
                    sourceId = sourceId,
                    isSuccess = false,
                    pingMs = ping,
                    popularCount = 0,
                    failureReason = reason,
                    message = "Failed: ${reason.description} (${res.exceptionOrNull()?.message})"
                )
            }
        } catch (e: Exception) {
            val ping = System.currentTimeMillis() - start
            val reason = classifyFailure(e)
            recordFailure(sourceId, reason, e.message)
            DiagnosticResult(
                sourceId = sourceId,
                isSuccess = false,
                pingMs = ping,
                popularCount = 0,
                failureReason = reason,
                message = "Exception: ${reason.description} (${e.localizedMessage})"
            )
        }
    }

    fun resetHealth(sourceId: String) {
        healthRecords[sourceId] = SourceHealthRecord(sourceId = sourceId, status = SourceHealthStatus.HEALTHY)
        _healthFlow.value = healthRecords.toMap()
    }
}
