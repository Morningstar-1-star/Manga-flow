package com.example.data.download

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.data.local.KotatsuDatabase
import com.example.data.model.DownloadStatus
import com.example.data.sources.SourceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * ChapterDownloadWorker
 * Real WorkManager background worker that downloads actual page bytes,
 * stores them in local disk storage for offline reading, updates real-time progress,
 * and handles pause/cancellation/retries gracefully.
 */
class ChapterDownloadWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val chapterId = inputData.getString("chapterId") ?: return@withContext Result.failure()
        val mangaId = inputData.getString("mangaId") ?: return@withContext Result.failure()
        val sourceId = inputData.getString("sourceId") ?: return@withContext Result.failure()

        val database = KotatsuDatabase.getInstance(applicationContext)
        val downloadDao = database.downloadDao()
        val chapterDao = database.chapterDao()
        val sourceManager = SourceManager(database.sourceConfigDao())

        val downloadId = "${mangaId}_$chapterId"

        try {
            downloadDao.updateStatus(downloadId, DownloadStatus.DOWNLOADING.name)

            val parser = sourceManager.getParser(sourceId)
            val pagesResult = parser.getPages(chapterId)
            if (pagesResult.isFailure) {
                downloadDao.updateStatus(downloadId, DownloadStatus.FAILED.name)
                return@withContext Result.retry()
            }

            val pages = pagesResult.getOrThrow()
            if (pages.isEmpty()) {
                downloadDao.updateStatus(downloadId, DownloadStatus.FAILED.name)
                return@withContext Result.failure()
            }

            // Create private local storage folder for this chapter
            val cleanManga = mangaId.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val cleanChapter = chapterId.replace(Regex("[^a-zA-Z0-9._-]"), "_")
            val chapterDir = File(applicationContext.filesDir, "downloads/$cleanManga/$cleanChapter")
            if (!chapterDir.exists()) {
                chapterDir.mkdirs()
            }

            val totalPages = pages.size
            var totalBytesDownloaded = 0L
            val startTime = System.currentTimeMillis()

            for ((index, page) in pages.withIndex()) {
                if (isStopped) {
                    downloadDao.updateStatus(downloadId, DownloadStatus.PAUSED.name)
                    return@withContext Result.success()
                }

                val ext = when {
                    page.imageUrl.contains(".png", ignoreCase = true) -> "png"
                    page.imageUrl.contains(".webp", ignoreCase = true) -> "webp"
                    else -> "jpg"
                }
                val outputFile = File(chapterDir, "page_%03d.$ext".format(index + 1))

                if (!outputFile.exists() || outputFile.length() == 0L) {
                    val requestBuilder = Request.Builder().url(page.imageUrl)
                    page.headers.forEach { (k, v) -> requestBuilder.header(k, v) }
                    requestBuilder.header("User-Agent", "MangaFlow/2.0 (Kotatsu)")
                    val response = httpClient.newCall(requestBuilder.build()).execute()
                    if (!response.isSuccessful) {
                        throw IOException("HTTP ${response.code} downloading page ${index + 1}")
                    }

                    val body = response.body ?: throw IOException("Empty response downloading page ${index + 1}")
                    val bytes = body.bytes()
                    FileOutputStream(outputFile).use { it.write(bytes) }
                    totalBytesDownloaded += bytes.size
                }

                val progress = (index + 1).toFloat() / totalPages.toFloat()
                val elapsedSec = ((System.currentTimeMillis() - startTime) / 1000).coerceAtLeast(1)
                val speedBytesPerSec = totalBytesDownloaded / elapsedSec
                val speedText = "%.1f MB/s".format(speedBytesPerSec / (1024.0 * 1024.0))
                val remainingPages = totalPages - (index + 1)
                val etaSeconds = if (index > 0) (elapsedSec / (index + 1)) * remainingPages else 0
                val etaText = if (etaSeconds > 60) "${etaSeconds / 60}m left" else "${etaSeconds}s left"

                downloadDao.updateProgress(
                    id = downloadId,
                    status = DownloadStatus.DOWNLOADING.name,
                    progress = progress,
                    speed = speedText,
                    eta = etaText
                )
                chapterDao.updateDownloadStatus(chapterId, isDownloaded = false, progress = (progress * 100).toInt())
                setProgress(workDataOf("progress" to (progress * 100).toInt()))
            }

            // Mark completed
            downloadDao.updateProgress(
                id = downloadId,
                status = DownloadStatus.COMPLETED.name,
                progress = 1.0f,
                speed = "Downloaded",
                eta = "Complete"
            )
            chapterDao.updateDownloadStatus(chapterId, isDownloaded = true, progress = 100)
            Result.success()
        } catch (e: Exception) {
            downloadDao.updateStatus(downloadId, DownloadStatus.FAILED.name)
            Result.failure()
        }
    }
}
