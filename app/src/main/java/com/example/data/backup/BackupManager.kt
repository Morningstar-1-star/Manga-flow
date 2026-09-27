package com.example.data.backup

import android.content.Context
import android.net.Uri
import com.example.data.local.BookmarkEntity
import com.example.data.local.HistoryEntity
import com.example.data.local.MangaEntity
import com.example.data.model.AppSettings
import com.example.data.repository.MangaRepository
import com.example.ui.viewmodel.MangaViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import java.util.zip.GZIPInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream

object BackupManager {

    data class ImportResult(
        val success: Boolean,
        val libraryCount: Int = 0,
        val historyCount: Int = 0,
        val bookmarkCount: Int = 0,
        val message: String = ""
    )

    suspend fun importBackup(
        context: Context,
        uri: Uri,
        repository: MangaRepository,
        viewModel: MangaViewModel
    ): ImportResult = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            val inputStream = contentResolver.openInputStream(uri) ?: return@withContext ImportResult(false, message = "Could not open file stream")

            var totalLibrary = 0
            var totalHistory = 0
            var totalBookmarks = 0

            val mangaEntities = mutableListOf<MangaEntity>()
            val historyEntities = mutableListOf<HistoryEntity>()
            val bookmarkEntities = mutableListOf<BookmarkEntity>()

            // Try reading as ZIP or plain JSON
            val bytes = inputStream.readBytes()
            inputStream.close()

            if (isZipFile(bytes)) {
                val zis = ZipInputStream(ByteArrayInputStream(bytes))
                var entry: ZipEntry? = zis.nextEntry
                while (entry != null) {
                    val entryName = entry.name.lowercase().trimStart('/', '\\')
                    val entryBytes = zis.readBytes()
                    val contentString = decompressIfNeeded(entryBytes)

                    when {
                        entryName.contains("favour") || entryName.contains("favorite") || entryName.contains("library") -> {
                            val items = parseJsonArrayOrLines(contentString)
                            for (i in 0 until items.length()) {
                                val obj = items.optJSONObject(i) ?: continue
                                val entity = parseMangaEntity(obj, inLibrary = true)
                                mangaEntities.add(entity)
                            }
                        }
                        entryName.contains("history") -> {
                            val items = parseJsonArrayOrLines(contentString)
                            for (i in 0 until items.length()) {
                                val obj = items.optJSONObject(i) ?: continue
                                val (manga, history) = parseHistoryEntity(obj)
                                if (manga != null) mangaEntities.add(manga)
                                if (history != null) historyEntities.add(history)
                            }
                        }
                        entryName.contains("bookmark") -> {
                            val items = parseJsonArrayOrLines(contentString)
                            for (i in 0 until items.length()) {
                                val obj = items.optJSONObject(i) ?: continue
                                val bookmark = parseBookmarkEntity(obj)
                                if (bookmark != null) bookmarkEntities.add(bookmark)
                            }
                        }
                        entryName.contains("settings") -> {
                            try {
                                val jsonObj = JSONObject(contentString)
                                parseAndUpdateSettings(jsonObj, viewModel)
                            } catch (e: Exception) {
                                // Ignore settings error
                            }
                        }
                    }
                    zis.closeEntry()
                    entry = zis.nextEntry
                }
                zis.close()
            } else {
                // Single JSON file
                val contentString = String(bytes, Charsets.UTF_8)
                val items = parseJsonArrayOrLines(contentString)
                for (i in 0 until items.length()) {
                    val obj = items.optJSONObject(i) ?: continue
                    val entity = parseMangaEntity(obj, inLibrary = true)
                    mangaEntities.add(entity)
                }
            }

            if (mangaEntities.isNotEmpty()) {
                repository.insertMangaEntities(mangaEntities)
                totalLibrary = mangaEntities.count { it.inLibrary }
            }
            if (historyEntities.isNotEmpty()) {
                repository.insertHistoryEntities(historyEntities)
                totalHistory = historyEntities.size
            }
            if (bookmarkEntities.isNotEmpty()) {
                repository.insertBookmarkEntities(bookmarkEntities)
                totalBookmarks = bookmarkEntities.size
            }

            ImportResult(
                success = true,
                libraryCount = totalLibrary,
                historyCount = totalHistory,
                bookmarkCount = totalBookmarks,
                message = "Restored $totalLibrary favorites, $totalHistory history & $totalBookmarks bookmarks"
            )
        } catch (e: Exception) {
            e.printStackTrace()
            ImportResult(false, message = "Import failed: ${e.localizedMessage ?: "Invalid file format"}")
        }
    }

    suspend fun exportBackup(
        context: Context,
        uri: Uri,
        repository: MangaRepository,
        settings: AppSettings
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val outputStream: OutputStream = context.contentResolver.openOutputStream(uri) ?: return@withContext false
            val zos = ZipOutputStream(outputStream)

            // 1. Export Favourites / Library
            val libraryList = repository.getAllLibraryMangaSync()
            val libraryArray = JSONArray()
            libraryList.forEach { m ->
                val mangaObj = JSONObject().apply {
                    put("id", m.id)
                    put("source", m.sourceId)
                    put("title", m.title)
                    put("alt_title", m.altTitle)
                    put("author", m.author)
                    put("artist", m.artist)
                    put("description", m.description)
                    put("cover_url", m.coverUrl)
                    put("status", m.status)
                    put("rating", m.rating)
                }
                val favObj = JSONObject().apply {
                    put("manga", mangaObj)
                    put("category", m.category)
                    put("created_at", System.currentTimeMillis())
                }
                libraryArray.put(favObj)
            }
            addZipEntry(zos, "favourites.json", libraryArray.toString(2))

            // 2. Export History
            val historyList = repository.getAllHistorySync()
            val historyArray = JSONArray()
            historyList.forEach { h ->
                val item = JSONObject().apply {
                    put("manga_id", h.mangaId)
                    put("manga_title", h.title)
                    put("cover_url", h.coverUrl)
                    put("chapter_id", h.chapterId)
                    put("chapter_name", h.chapterName)
                    put("read_page", h.readPage)
                    put("total_pages", h.totalPages)
                    put("updated_at", h.timestamp)
                }
                historyArray.put(item)
            }
            addZipEntry(zos, "history.json", historyArray.toString(2))

            // 3. Export Bookmarks
            val bookmarkList = repository.getAllBookmarksSync()
            val bookmarkArray = JSONArray()
            bookmarkList.forEach { b ->
                val item = JSONObject().apply {
                    put("id", b.id)
                    put("manga_id", b.mangaId)
                    put("manga_title", b.mangaTitle)
                    put("chapter_id", b.chapterId)
                    put("chapter_name", b.chapterName)
                    put("cover_url", b.coverUrl)
                    put("page", b.pageNumber)
                    put("created_at", b.timestamp)
                }
                bookmarkArray.put(item)
            }
            addZipEntry(zos, "bookmarks.json", bookmarkArray.toString(2))

            // 4. Export Settings
            val settingsObj = JSONObject().apply {
                put("color_scheme", settings.colorScheme)
                put("theme_mode", settings.themeMode)
                put("is_amoled_black", settings.isAmoledBlack)
                put("is_grid_mode", settings.isGridMode)
                put("reader_mode", settings.defaultReaderMode)
            }
            addZipEntry(zos, "settings.json", settingsObj.toString(2))

            // 5. Identity
            val identityObj = JSONObject().apply {
                put("app", "Kotatsu")
                put("version", 1)
                put("created_at", System.currentTimeMillis())
            }
            addZipEntry(zos, "identity.json", identityObj.toString(2))

            zos.finish()
            zos.close()
            outputStream.close()
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    private fun addZipEntry(zos: ZipOutputStream, entryName: String, content: String) {
        val entry = ZipEntry(entryName)
        zos.putNextEntry(entry)
        zos.write(content.toByteArray(Charsets.UTF_8))
        zos.closeEntry()
    }

    private fun isZipFile(bytes: ByteArray): Boolean {
        if (bytes.size < 4) return false
        return bytes[0] == 0x50.toByte() && bytes[1] == 0x4B.toByte() &&
                bytes[2] == 0x03.toByte() && bytes[3] == 0x04.toByte()
    }

    private fun decompressIfNeeded(bytes: ByteArray): String {
        if (bytes.size >= 2 && bytes[0] == 0x1f.toByte() && bytes[1] == 0x8b.toByte()) {
            return try {
                val gzis = GZIPInputStream(ByteArrayInputStream(bytes))
                val out = ByteArrayOutputStream()
                gzis.copyTo(out)
                gzis.close()
                String(out.toByteArray(), Charsets.UTF_8)
            } catch (e: Exception) {
                String(bytes, Charsets.UTF_8)
            }
        }
        return String(bytes, Charsets.UTF_8)
    }

    private fun parseJsonArrayOrLines(content: String): JSONArray {
        val trimmed = content.trim()
        if (trimmed.startsWith("[")) {
            return JSONArray(trimmed)
        } else if (trimmed.startsWith("{")) {
            val arr = JSONArray()
            // Check if wrapped in object e.g. {"favourites": [...]}
            val root = JSONObject(trimmed)
            val keys = root.keys()
            var foundArray = false
            while (keys.hasNext()) {
                val k = keys.next()
                val valObj = root.optJSONArray(k)
                if (valObj != null) {
                    return valObj
                }
            }
            if (!foundArray) {
                // Line delimited JSON or single object
                arr.put(root)
            }
            return arr
        } else {
            // Line delimited
            val arr = JSONArray()
            trimmed.lines().forEach { line ->
                if (line.trim().startsWith("{")) {
                    try {
                        arr.put(JSONObject(line.trim()))
                    } catch (e: Exception) {}
                }
            }
            return arr
        }
    }

    private fun parseMangaEntity(obj: JSONObject, inLibrary: Boolean): MangaEntity {
        val mangaObj = obj.optJSONObject("manga") ?: obj
        val id = mangaObj.optString("id", mangaObj.optString("manga_id", mangaObj.optString("mangaId", "manga_${System.currentTimeMillis()}")))
        val title = mangaObj.optString("title", mangaObj.optString("manga_title", mangaObj.optString("name", "Unknown Manga")))
        val altTitle = mangaObj.optString("alt_title", mangaObj.optString("altTitle", ""))
        val coverUrl = mangaObj.optString("cover_url", mangaObj.optString("coverUrl", mangaObj.optString("public_url", mangaObj.optString("url", ""))))
        val sourceId = mangaObj.optString("source", mangaObj.optString("source_id", mangaObj.optString("sourceId", "mangadex")))
        val categoryName = obj.optString("category", obj.optString("category_name", obj.optString("categoryName", "Favorites")))
        val author = mangaObj.optString("author", mangaObj.optString("artist", ""))
        val description = mangaObj.optString("description", mangaObj.optString("summary", ""))
        val status = mangaObj.optString("status", mangaObj.optString("state", "Ongoing"))
        val rating = mangaObj.optDouble("rating", 4.5).toFloat()

        return MangaEntity(
            id = id,
            sourceId = sourceId,
            title = title,
            altTitle = altTitle,
            author = author,
            artist = "",
            description = description,
            coverUrl = coverUrl,
            status = status,
            rating = rating,
            isNsfw = false,
            genresString = "Manga",
            category = categoryName,
            inLibrary = inLibrary,
            lastReadChapterId = null,
            lastReadChapterName = null,
            lastReadPage = 0,
            lastReadTime = System.currentTimeMillis(),
            totalChapters = 20,
            readProgressPercent = 0
        )
    }

    private fun parseHistoryEntity(obj: JSONObject): Pair<MangaEntity?, HistoryEntity?> {
        val mangaObj = obj.optJSONObject("manga") ?: obj
        val mangaId = mangaObj.optString("id", obj.optString("manga_id", obj.optString("mangaId", "")))
        if (mangaId.isEmpty()) return Pair(null, null)

        val mangaTitle = mangaObj.optString("title", obj.optString("manga_title", obj.optString("title", "Unknown Manga")))
        val coverUrl = mangaObj.optString("cover_url", obj.optString("cover_url", obj.optString("coverUrl", "")))
        val sourceId = mangaObj.optString("source", obj.optString("source", "mangadex"))

        val chapterObj = obj.optJSONObject("chapter")
        val chapterId = chapterObj?.optString("id") ?: obj.optString("chapter_id", obj.optString("chapterId", "ch_1"))
        val chapterName = chapterObj?.optString("title") ?: obj.optString("chapter_name", obj.optString("chapter_title", obj.optString("chapterName", "Chapter 1")))

        val page = obj.optInt("read_page", obj.optInt("page", obj.optInt("readPage", 1)))
        val totalPages = obj.optInt("total_pages", obj.optInt("totalPages", obj.optInt("pages_count", 20)))
        val time = obj.optLong("updated_at", obj.optLong("timestamp", obj.optLong("date", System.currentTimeMillis())))
        val percent = if (totalPages > 0) ((page.toFloat() / totalPages) * 100).toInt() else 0

        val manga = MangaEntity(
            id = mangaId,
            sourceId = sourceId,
            title = mangaTitle,
            altTitle = "",
            author = "",
            artist = "",
            description = "",
            coverUrl = coverUrl,
            status = "Ongoing",
            rating = 4.5f,
            isNsfw = false,
            genresString = "Manga",
            category = "Favorites",
            inLibrary = true,
            lastReadChapterId = chapterId,
            lastReadChapterName = chapterName,
            lastReadPage = page,
            lastReadTime = time,
            totalChapters = totalPages,
            readProgressPercent = percent
        )

        val history = HistoryEntity(
            mangaId = mangaId,
            title = mangaTitle,
            coverUrl = coverUrl,
            chapterId = chapterId,
            chapterName = chapterName,
            readPage = page,
            totalPages = totalPages,
            timestamp = time,
            progressPercent = percent,
            isCompleted = page >= totalPages
        )

        return Pair(manga, history)
    }

    private fun parseBookmarkEntity(obj: JSONObject): BookmarkEntity? {
        val mangaId = obj.optString("manga_id", obj.optString("mangaId", ""))
        val chapterId = obj.optString("chapter_id", obj.optString("chapterId", "ch_1"))
        val page = obj.optInt("page", obj.optInt("page_number", obj.optInt("pageNumber", 1)))
        val id = obj.optString("id", "${mangaId}_${chapterId}_$page")
        val mangaTitle = obj.optString("manga_title", obj.optString("mangaTitle", "Unknown"))
        val chapterName = obj.optString("chapter_name", obj.optString("chapterName", "Chapter 1"))
        val coverUrl = obj.optString("cover_url", obj.optString("coverUrl", ""))
        val time = obj.optLong("created_at", obj.optLong("timestamp", System.currentTimeMillis()))

        if (mangaId.isEmpty()) return null

        return BookmarkEntity(
            id = id,
            mangaId = mangaId,
            mangaTitle = mangaTitle,
            chapterId = chapterId,
            chapterName = chapterName,
            coverUrl = coverUrl,
            pageNumber = page,
            timestamp = time
        )
    }

    private fun parseAndUpdateSettings(jsonObj: JSONObject, viewModel: MangaViewModel) {
        viewModel.updateSettings { current ->
            current.copy(
                colorScheme = jsonObj.optString("color_scheme", current.colorScheme),
                themeMode = jsonObj.optString("theme_mode", current.themeMode),
                isAmoledBlack = jsonObj.optBoolean("is_amoled_black", jsonObj.optBoolean("amoled", current.isAmoledBlack)),
                isGridMode = jsonObj.optBoolean("is_grid_mode", current.isGridMode),
                defaultReaderMode = jsonObj.optString("reader_mode", current.defaultReaderMode)
            )
        }
    }
}
