package com.example

import com.example.data.sources.*
import kotlinx.coroutines.runBlocking
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testSources() = runBlocking {
        println("=== TESTING GUYA ===")
        val guya = GuyaCubariSource()
        val guyaRes = guya.getPopularManga(1)
        println("Guya popular: success=${guyaRes.isSuccess}, count=${guyaRes.getOrNull()?.size}, error=${guyaRes.exceptionOrNull()}")
        guyaRes.getOrNull()?.take(2)?.forEach { println("  Guya: ${it.title} (${it.id}) - ${it.coverUrl}") }

        println("=== TESTING MANGADEX ===")
        val mangadex = MangaDexSource()
        val mdRes = mangadex.getPopularManga(1)
        println("MangaDex popular: success=${mdRes.isSuccess}, count=${mdRes.getOrNull()?.size}, error=${mdRes.exceptionOrNull()}")
        mdRes.getOrNull()?.take(2)?.forEach { println("  MangaDex: ${it.title} (${it.id}) - ${it.coverUrl}") }

        println("=== TESTING COMICK ===")
        val comick = ComickSource()
        val comickRes = comick.getPopularManga(1)
        println("ComicK popular: success=${comickRes.isSuccess}, count=${comickRes.getOrNull()?.size}, error=${comickRes.exceptionOrNull()}")

        println("=== TESTING CUUTRUYEN ===")
        val cuuTruyen = CuuTruyenSource()
        val ctRes = cuuTruyen.getPopularManga(1)
        println("CuuTruyen popular: success=${ctRes.isSuccess}, count=${ctRes.getOrNull()?.size}, error=${ctRes.exceptionOrNull()}")

        println("=== TESTING MANGANATO ===")
        val nato = MangaNatoSource()
        val natoRes = nato.getPopularManga(1)
        println("MangaNato popular: success=${natoRes.isSuccess}, count=${natoRes.getOrNull()?.size}, error=${natoRes.exceptionOrNull()}")
    }
}

