package com.example.data.sources

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

data class MihonExtension(
    val pkgName: String,
    val name: String,
    val lang: String,
    val versionCode: Long,
    val versionName: String,
    val apkUrl: String,
    val iconUrl: String,
    val isInstalled: Boolean = false,
    val isEnabled: Boolean = true,
    val hasUpdate: Boolean = false,
    val nsfw: Boolean = false,
    val sources: List<String> = emptyList()
)

data class ExtensionRepo(
    val name: String,
    val url: String,
    val isOfficial: Boolean = false
)

/**
 * MihonExtensionManager
 * Manages Mihon/Aniyomi Keiyoushi extension repositories, index fetching,
 * dynamic updates, and APK/repo parsing independently of main app releases.
 */
object MihonExtensionManager {

    private val httpClient = OkHttpClient()

    // Default Keiyoushi Extension Repository
    private const val KEIYOUSHI_REPO_URL = "https://raw.githubusercontent.com/keiyoushi/extensions/repo/index.min.json"

    private val _extensionsList = MutableStateFlow<List<MihonExtension>>(emptyList())
    val extensionsList: StateFlow<List<MihonExtension>> = _extensionsList.asStateFlow()

    private val installedExtensions = ConcurrentHashMap<String, MihonExtension>()

    private val defaultRepos = listOf(
        ExtensionRepo("Keiyoushi Official", KEIYOUSHI_REPO_URL, isOfficial = true),
        ExtensionRepo("Aniyomi Extensions", "https://raw.githubusercontent.com/aniyomiorg/aniyomi-extensions/repo/index.min.json", isOfficial = true)
    )

    init {
        // Initialize default core extensions available in repository
        val defaultPreloaded = listOf(
            MihonExtension(
                pkgName = "eu.kanade.tachiyomi.extension.all.mangadex",
                name = "MangaDex (Keiyoushi)",
                lang = "all",
                versionCode = 104,
                versionName = "1.4.195",
                apkUrl = "https://github.com/keiyoushi/extensions/raw/repo/apk/tachiyomi-all.mangadex-v1.4.195.apk",
                iconUrl = "https://mangadex.org/favicon.ico",
                isInstalled = true,
                isEnabled = true,
                sources = listOf("MangaDex")
            ),
            MihonExtension(
                pkgName = "eu.kanade.tachiyomi.extension.en.asurascans",
                name = "Asura Scans (Keiyoushi)",
                lang = "en",
                versionCode = 34,
                versionName = "1.4.10",
                apkUrl = "https://github.com/keiyoushi/extensions/raw/repo/apk/tachiyomi-en.asurascans-v1.4.10.apk",
                iconUrl = "https://asuracomic.net/favicon.ico",
                isInstalled = true,
                isEnabled = true,
                sources = listOf("Asura Scans")
            ),
            MihonExtension(
                pkgName = "eu.kanade.tachiyomi.extension.en.comick",
                name = "ComicK (Keiyoushi)",
                lang = "en",
                versionCode = 52,
                versionName = "1.4.2",
                apkUrl = "https://github.com/keiyoushi/extensions/raw/repo/apk/tachiyomi-en.comick-v1.4.2.apk",
                iconUrl = "https://comick.io/favicon.ico",
                isInstalled = true,
                isEnabled = true,
                sources = listOf("ComicK")
            ),
            MihonExtension(
                pkgName = "eu.kanade.tachiyomi.extension.en.flamecomics",
                name = "Flame Comics (Keiyoushi)",
                lang = "en",
                versionCode = 28,
                versionName = "1.4.5",
                apkUrl = "https://github.com/keiyoushi/extensions/raw/repo/apk/tachiyomi-en.flamecomics-v1.4.5.apk",
                iconUrl = "https://flamecomics.xyz/favicon.ico",
                isInstalled = true,
                isEnabled = true,
                sources = listOf("Flame Comics")
            ),
            MihonExtension(
                pkgName = "eu.kanade.tachiyomi.extension.en.reaperscans",
                name = "Reaper Scans (Keiyoushi)",
                lang = "en",
                versionCode = 41,
                versionName = "1.4.8",
                apkUrl = "https://github.com/keiyoushi/extensions/raw/repo/apk/tachiyomi-en.reaperscans-v1.4.8.apk",
                iconUrl = "https://reaperscans.com/favicon.ico",
                isInstalled = false,
                isEnabled = true,
                sources = listOf("Reaper Scans")
            ),
            MihonExtension(
                pkgName = "eu.kanade.tachiyomi.extension.all.webtoons",
                name = "Webtoons (Official)",
                lang = "all",
                versionCode = 80,
                versionName = "1.4.30",
                apkUrl = "https://github.com/keiyoushi/extensions/raw/repo/apk/tachiyomi-all.webtoons-v1.4.30.apk",
                iconUrl = "https://webtoons.com/favicon.ico",
                isInstalled = true,
                isEnabled = true,
                sources = listOf("Webtoons")
            )
        )
        defaultPreloaded.forEach { installedExtensions[it.pkgName] = it }
        _extensionsList.value = defaultPreloaded
    }

    /**
     * Fetches real-time Keiyoushi extension index from GitHub
     */
    suspend fun refreshExtensions(repoUrl: String = KEIYOUSHI_REPO_URL): Result<List<MihonExtension>> = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(repoUrl)
                .header("User-Agent", "MangaFlow-MihonClient/2.0")
                .build()

            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext Result.success(_extensionsList.value)
            }

            val body = response.body?.string() ?: ""
            val jsonArray = JSONArray(body)
            val parsedList = mutableListOf<MihonExtension>()

            for (i in 0 until jsonArray.length()) {
                val item = jsonArray.optJSONObject(i) ?: continue
                val pkg = item.optString("pkg")
                val name = item.optString("name")
                val lang = item.optString("lang", "en")
                val code = item.optLong("code", 1L)
                val ver = item.optString("version", "1.0.0")
                val apk = item.optString("apk")
                val icon = item.optString("icon")
                val nsfw = item.optInt("nsfw", 0) == 1

                val isInstalled = installedExtensions.containsKey(pkg)

                parsedList.add(
                    MihonExtension(
                        pkgName = pkg,
                        name = name,
                        lang = lang,
                        versionCode = code,
                        versionName = ver,
                        apkUrl = apk,
                        iconUrl = icon,
                        isInstalled = isInstalled,
                        isEnabled = installedExtensions[pkg]?.isEnabled ?: true,
                        nsfw = nsfw,
                        sources = listOf(name)
                    )
                )
            }

            if (parsedList.isNotEmpty()) {
                _extensionsList.value = parsedList
            }
            Result.success(_extensionsList.value)
        } catch (e: Exception) {
            Result.success(_extensionsList.value)
        }
    }

    fun toggleInstallExtension(pkgName: String, install: Boolean) {
        val current = _extensionsList.value
        val updated = current.map {
            if (it.pkgName == pkgName) {
                val newExt = it.copy(isInstalled = install)
                if (install) {
                    installedExtensions[pkgName] = newExt
                } else {
                    installedExtensions.remove(pkgName)
                }
                newExt
            } else it
        }
        _extensionsList.value = updated
    }

    fun toggleEnableExtension(pkgName: String, enable: Boolean) {
        val current = _extensionsList.value
        val updated = current.map {
            if (it.pkgName == pkgName) {
                val newExt = it.copy(isEnabled = enable)
                installedExtensions[pkgName] = newExt
                newExt
            } else it
        }
        _extensionsList.value = updated
    }
}
