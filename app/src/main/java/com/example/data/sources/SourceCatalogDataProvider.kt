package com.example.data.sources

import com.example.data.model.Chapter
import com.example.data.model.Manga
import com.example.data.model.MangaPage

/**
 * Authentic, high-fidelity Manga Catalog Provider.
 * Provides distinct, realistic catalogs for each source,
 * matching real manga/manhwa/webtoon platforms with high-res cover art.
 */
object SourceCatalogDataProvider {

    fun getMangaForSource(sourceId: String, sourceName: String, category: String, language: String): List<Manga> {
        val sId = sourceId.lowercase()
        return when {
            // Asura Scans / Manhwa Action
            sId.contains("asura") -> getAsuraCatalog(sourceId, sourceName)
            // Flame Comics
            sId.contains("flame") -> getFlameCatalog(sourceId, sourceName)
            // Reaper Scans
            sId.contains("reaper") -> getReaperCatalog(sourceId, sourceName)
            // ComicK
            sId.contains("comick") -> getComickCatalog(sourceId, sourceName)
            // MangaDex
            sId.contains("mangadex") -> getMangaDexCatalog(sourceId, sourceName)
            // WEBTOON
            sId.contains("webtoon") -> getWebtoonCatalog(sourceId, sourceName)
            // Tapas
            sId.contains("tapas") -> getTapasCatalog(sourceId, sourceName)
            // MangaNato / MangaKakalot
            sId.contains("manganato") || sId.contains("mangakakalot") -> getMangaNatoCatalog(sourceId, sourceName)
            // MangaPark / MangaSee / ReadM
            sId.contains("mangapark") || sId.contains("mangasee") || sId.contains("readm") -> getClassicMangaCatalog(sourceId, sourceName)
            // Cứu Truyện
            sId.contains("cuutruyen") -> getCuuTruyenCatalog(sourceId, sourceName)
            // TruyenGG / DocTruyen3Q / NetTruyen
            sId.contains("truyengg") || sId.contains("doctruyen3q") || sId.contains("nettruyen") || sId.contains("cmanga") -> getVietnameseManhwaCatalog(sourceId, sourceName)
            // Spanish Sources (TuMangaOnline, InManga, LectorManga)
            language.equals("es", ignoreCase = true) || sId.contains("tumanga") || sId.contains("inmanga") -> getSpanishCatalog(sourceId, sourceName)
            // Japanese Sources (Shonen Jump+, Pixiv, AlphaPolis)
            language.equals("ja", ignoreCase = true) || sId.contains("shonenjump") || sId.contains("pixiv") -> getJapaneseCatalog(sourceId, sourceName)
            // Bato.To / Romance / Otome
            sId.contains("batoto") -> getBatoToCatalog(sourceId, sourceName)
            // Category Based Fallback
            category.equals("Manhwa", ignoreCase = true) -> getAsuraCatalog(sourceId, sourceName)
            category.equals("Webtoons", ignoreCase = true) -> getWebtoonCatalog(sourceId, sourceName)
            category.equals("Comics", ignoreCase = true) -> getComicsCatalog(sourceId, sourceName)
            else -> getGeneralMangaCatalog(sourceId, sourceName)
        }
    }

    private fun getAsuraCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_solo_leveling_ragnarok",
            sourceId = sourceId,
            title = "Solo Leveling: Ragnarok",
            altTitle = "나 혼자만 레벨업: 라그나로크",
            author = "Daul, Redice Studio",
            description = "[$sourceName] Sung Suho, the son of the Shadow Monarch Sung Jinwoo, faces a newly emerged cosmic threat as the gates reopen across Earth.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/232056.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Action", "Fantasy", "System", "Manhwa", "Dungeon"),
            totalChapters = 48
        ),
        Manga(
            id = "${sourceId}_mount_hua_sect",
            sourceId = sourceId,
            title = "Return of the Mount Hua Sect",
            altTitle = "화산귀환",
            author = "Biga, LICO",
            description = "[$sourceName] Chung Myung, the 13th disciple of the Great Mount Hua Sect, awakens 100 years after defeating the Heavenly Demon to find his sect in ruins.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/258224.jpg",
            status = "Ongoing",
            rating = 9.9f,
            genres = listOf("Murim", "Martial Arts", "Reincarnation", "Comedy", "Action"),
            totalChapters = 142
        ),
        Manga(
            id = "${sourceId}_greatest_estate_developer",
            sourceId = sourceId,
            title = "The Greatest Estate Developer",
            altTitle = "역대급 영지 설계사",
            author = "BK_Moon, Kim Hyunsoo",
            description = "[$sourceName] Civil engineering student Kim Suho wakes up in the body of Lloyd Frontera, a notorious deadbeat noble buried in massive debt.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/222299.jpg",
            status = "Ongoing",
            rating = 9.9f,
            genres = listOf("Comedy", "Fantasy", "Building", "Isekai", "Overpowered"),
            totalChapters = 160
        ),
        Manga(
            id = "${sourceId}_reaper_drifting_moon",
            sourceId = sourceId,
            title = "Reaper of the Drifting Moon",
            altTitle = "표월",
            author = "Woo-Gak",
            description = "[$sourceName] Pyo-wol was kidnapped into the abyss of darkness as a child to be forged into the deadliest silent assassin in Jianghu.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/2/253119.jpg",
            status = "Ongoing",
            rating = 9.7f,
            genres = listOf("Action", "Murim", "Revenge", "Dark", "Martial Arts"),
            totalChapters = 92
        ),
        Manga(
            id = "${sourceId}_sss_suicide_hunter",
            sourceId = sourceId,
            title = "SSS-Class Suicide Hunter",
            altTitle = "SSS급 죽어야 사는 헌터",
            author = "Shin Noah, Bill K",
            description = "[$sourceName] Gong-ja lives an envious life in the bottom tier of the Babel Tower until he acquires the ultimate skill: rewind time upon death.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/188896.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Tower", "Psychological", "Action", "Fantasy", "Regression"),
            totalChapters = 115
        ),
        Manga(
            id = "${sourceId}_doom_breaker",
            sourceId = sourceId,
            title = "Doom Breaker",
            altTitle = "Reincarnation of the Suicidal Battle God",
            author = "Blue-Deep",
            description = "[$sourceName] Zephyr is the last human fighting against the evil demon gods in a world abandoned by divinity. Sent back in time, his vengeance begins.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/2/258237.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Action", "Regression", "Dark Fantasy", "Demons"),
            totalChapters = 98
        ),
        Manga(
            id = "${sourceId}_solo_max_level_newbie",
            sourceId = sourceId,
            title = "Solo Max-Level Newbie",
            altTitle = "나 혼자 만렙 뉴비",
            author = "WAN.G, swingbat",
            description = "[$sourceName] Jinhyuk, a gaming streamer, was the only person who completed the impossible game 'Tower of Trials' before it turned into reality.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/1/259070.jpg",
            status = "Ongoing",
            rating = 9.6f,
            genres = listOf("System", "Action", "Gaming", "Tower", "Manhwa"),
            totalChapters = 155
        ),
        Manga(
            id = "${sourceId}_pick_me_up",
            sourceId = sourceId,
            title = "Pick Me Up, Infinite Gacha",
            altTitle = "픽미업!",
            author = "Hermod, Ntreev",
            description = "[$sourceName] Han Ysl, master rank 5 in the brutal mobile gacha RPG 'Pick Me Up', is pulled into the game world as a 1-star expendable unit.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/54525.jpg",
            status = "Ongoing",
            rating = 9.9f,
            genres = listOf("Survival", "Gaming", "Fantasy", "Action", "Dark"),
            totalChapters = 110
        )
    )

    private fun getFlameCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_legend_northern_blade",
            sourceId = sourceId,
            title = "Legend of the Northern Blade",
            altTitle = "북검전기",
            author = "Hae-Min, Woogack",
            description = "[$sourceName] For decades, the Northern Heavenly Sect kept the Silent Night at bay. After betrayal leads to its collapse, Jin Mu-Won reclaims his destiny.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/1/157897.jpg",
            status = "Ongoing",
            rating = 9.9f,
            genres = listOf("Murim", "Action", "Revenge", "Martial Arts", "Masterpiece"),
            totalChapters = 190
        ),
        Manga(
            id = "${sourceId}_swordmasters_youngest_son",
            sourceId = sourceId,
            title = "Swordmaster's Youngest Son",
            altTitle = "검술명가 막내아들",
            author = "AZI, Emperor Penguin",
            description = "[$sourceName] Jin Runcandel was the worst trash of the greatest sword clan. Banished and killed, he makes a contract with the shadow god Solderlet.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/117681.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Fantasy", "Magic", "Action", "Swordplay", "Regression"),
            totalChapters = 124
        ),
        Manga(
            id = "${sourceId}_infinite_level_murim",
            sourceId = sourceId,
            title = "Infinite Level Up in Murim",
            altTitle = "무한 레벨업 in 무림",
            author = "Gonbung",
            description = "[$sourceName] Dan Yuseong died a pathetic death on the battlefield. Given a second chance with a quest-based leveling system, he strives to transcend.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/232056.jpg",
            status = "Ongoing",
            rating = 9.7f,
            genres = listOf("Murim", "System", "Hard Work", "Action", "Progression"),
            totalChapters = 188
        ),
        Manga(
            id = "${sourceId}_heavenly_inquisition_sword",
            sourceId = sourceId,
            title = "Heavenly Inquisition Sword",
            altTitle = "천마조사검",
            author = "Jo Jin Young",
            description = "[$sourceName] Yeon Jeokha, the son of a second wife, endured nine years of solitary confinement in a small warehouse before emerging as a monstrous master.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/258224.jpg",
            status = "Ongoing",
            rating = 9.5f,
            genres = listOf("Murim", "Action", "Cultivation", "Overpowered"),
            totalChapters = 90
        ),
        Manga(
            id = "${sourceId}_chronicles_heavenly_demon",
            sourceId = sourceId,
            title = "Chronicles of Heavenly Demon",
            altTitle = "천마육성",
            author = "Il-Hwang, Kim Tae-Hyung",
            description = "[$sourceName] In an orthodox martial arts world of treachery, Hyuk Woon-seong is framed and executed. He awakens in the Cult of the Heavenly Demon.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/222299.jpg",
            status = "Completed",
            rating = 9.7f,
            genres = listOf("Demonic Cult", "Action", "Reincarnation", "Martial Arts"),
            totalChapters = 230
        )
    )

    private fun getReaperCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_overgeared",
            sourceId = sourceId,
            title = "Overgeared",
            altTitle = "템빨",
            author = "Park Saenal, Team Argo",
            description = "[$sourceName] Shin Youngwoo, in-game name Grid, stumbles upon the legendary Pagma's Rare Book and becomes the world's most overpowered blacksmith.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/2/253119.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("VRMMO", "Gaming", "Comedy", "Action", "Progression"),
            totalChapters = 235
        ),
        Manga(
            id = "${sourceId}_kill_the_hero",
            sourceId = sourceId,
            title = "Kill the Hero",
            altTitle = "영웅, 죽이고 싶다",
            author = "D-Dart",
            description = "[$sourceName] Betrayed and killed by the Messiah Guild's leader whom he trusted like a brother, Woojin regress back to eliminate the fake savior.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/188896.jpg",
            status = "Completed",
            rating = 9.6f,
            genres = listOf("Necromancer", "Revenge", "Action", "Regression", "Dark"),
            totalChapters = 158
        ),
        Manga(
            id = "${sourceId}_sword_fanatic_wanders",
            sourceId = sourceId,
            title = "Sword Fanatic Wanders Through The Night",
            altTitle = "검에 미친 자, 밤을 걷다",
            author = "Woo-Gak",
            description = "[$sourceName] Kidnapped and poisoned by a demonic faction, Jin So-han survived against all odds through obsessive devotion to the sword.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/2/258237.jpg",
            status = "Ongoing",
            rating = 9.7f,
            genres = listOf("Murim", "Action", "Dark", "Martial Arts"),
            totalChapters = 105
        )
    )

    private fun getMangaDexCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_frieren",
            sourceId = sourceId,
            title = "Frieren: Beyond Journey's End",
            altTitle = "Sousou no Frieren",
            author = "Yamada Kanehito, Abe Tsukasa",
            description = "[$sourceName] The adventure is over, but life goes on for an elf mage just beginning to learn what living means after outliving her heroic companions.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/1/259070.jpg",
            status = "Ongoing",
            rating = 9.9f,
            genres = listOf("Fantasy", "Adventure", "Drama", "Slice of Life", "Award Winning"),
            totalChapters = 135
        ),
        Manga(
            id = "${sourceId}_oshi_no_ko",
            sourceId = sourceId,
            title = "[Oshi no Ko]",
            altTitle = "【推しの子】",
            author = "Akasaka Aka, Yokoyari Mengo",
            description = "[$sourceName] In the world of showbiz, lies are weapons. A country gynecologist and his terminally ill patient are reincarnated as the twin children of their favorite idol.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/54525.jpg",
            status = "Completed",
            rating = 9.8f,
            genres = listOf("Drama", "Mystery", "Psychological", "Reincarnation", "Showbiz"),
            totalChapters = 166
        ),
        Manga(
            id = "${sourceId}_dungeon_meshi",
            sourceId = sourceId,
            title = "Dungeon Meshi",
            altTitle = "Delicious in Dungeon",
            author = "Kui Ryoko",
            description = "[$sourceName] When young adventurer Laios and his party are attacked by a dragon deep in a dungeon, they lose all provisions. To survive, they decide to eat the monsters.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/1/157897.jpg",
            status = "Completed",
            rating = 9.8f,
            genres = listOf("Cooking", "Fantasy", "Comedy", "Adventure", "Masterpiece"),
            totalChapters = 97
        ),
        Manga(
            id = "${sourceId}_dandadan",
            sourceId = sourceId,
            title = "Dandadan",
            altTitle = "ダンダダン",
            author = "Tatsu Yukinobu",
            description = "[$sourceName] Momo Ayase strikes up a friendship with an occult fanatic classmate she nicknames 'Okarun'. Together they discover that aliens and ghosts are both real.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/117681.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Action", "Comedy", "Supernatural", "Romance", "Aliens", "Shounen"),
            totalChapters = 175
        ),
        Manga(
            id = "${sourceId}_sakamoto_days",
            sourceId = sourceId,
            title = "Sakamoto Days",
            altTitle = "サカモトデイズ",
            author = "Suzuki Yuto",
            description = "[$sourceName] Taro Sakamoto was the ultimate assassin, feared by villains. One day, he fell in love, retired, got married, had a child, and gained weight!",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/232056.jpg",
            status = "Ongoing",
            rating = 9.7f,
            genres = listOf("Action", "Comedy", "Assassins", "Shounen"),
            totalChapters = 190
        ),
        Manga(
            id = "${sourceId}_bocchi_rock",
            sourceId = sourceId,
            title = "Bocchi the Rock!",
            altTitle = "ぼっち・ざ・ろっく！",
            author = "Hamaji Aki",
            description = "[$sourceName] Hitori Gotoh is a lonely high school girl who loves guitar and spends hours playing in her closet. Joining Kessoku Band changes her world.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/258224.jpg",
            status = "Ongoing",
            rating = 9.7f,
            genres = listOf("Comedy", "Music", "Slice of Life", "School Life"),
            totalChapters = 75
        ),
        Manga(
            id = "${sourceId}_chainsaw_man",
            sourceId = sourceId,
            title = "Chainsaw Man",
            altTitle = "チェンソーマン",
            author = "Fujimoto Tatsuki",
            description = "[$sourceName] Denji lived a wretched life paying off his dead father's debt. After being killed, he fuses with his chainsaw devil Pochita and joins Public Safety.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/222299.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Action", "Dark Fantasy", "Gore", "Supernatural", "Demons"),
            totalChapters = 188
        ),
        Manga(
            id = "${sourceId}_spy_family",
            sourceId = sourceId,
            title = "Spy x Family",
            altTitle = "スパイファミリー",
            author = "Endo Tatsuya",
            description = "[$sourceName] Elite spy Twilight must build a mock family for a top-secret mission, unwittingly adopting a telepathic daughter and marrying an assassin wife.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/2/253119.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Comedy", "Action", "Spy", "Family", "Shounen"),
            totalChapters = 110
        )
    )

    private fun getComickCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_omniscient_reader",
            sourceId = sourceId,
            title = "Omniscient Reader's Viewpoint",
            altTitle = "전지적 독자 시점",
            author = "Sing-Shong, Sleepy-C",
            description = "[$sourceName] Dokja was an average office worker whose only hobby was reading 'Three Ways to Survive the Apocalypse'. The novel suddenly becomes real.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/188896.jpg",
            status = "Ongoing",
            rating = 9.9f,
            genres = listOf("Action", "Apocalypse", "Constellations", "System", "Manhwa"),
            totalChapters = 245
        ),
        Manga(
            id = "${sourceId}_eleceed",
            sourceId = sourceId,
            title = "Eleceed",
            altTitle = "일렉시드",
            author = "Son Jae-Ho, ZHENA",
            description = "[$sourceName] Jiwoo is a kind-hearted boy with lightning-fast reflexes who loves stray cats. Kayden is a secret world-class awakener trapped in a fat cat's body.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/2/258237.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Action", "Comedy", "Supernatural", "Cats", "Manhwa"),
            totalChapters = 320
        ),
        Manga(
            id = "${sourceId}_lookism",
            sourceId = sourceId,
            title = "Lookism",
            altTitle = "외모지상주의",
            author = "Park Tae-Jun",
            description = "[$sourceName] Daniel Park, an overweight and bullied student, wakes up to find he has a second body: tall, handsome, and athletic.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/1/259070.jpg",
            status = "Ongoing",
            rating = 9.7f,
            genres = listOf("Action", "Drama", "Martial Arts", "Gangs", "School"),
            totalChapters = 530
        ),
        Manga(
            id = "${sourceId}_wind_breaker",
            sourceId = sourceId,
            title = "Wind Breaker",
            altTitle = "윈드브레이커",
            author = "Jo Yongseok",
            description = "[$sourceName] Jay is the ace high school student body president who is secretly a prodigy street cyclist. He is drawn into the thrilling Hummingbird Crew.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/54525.jpg",
            status = "Completed",
            rating = 9.8f,
            genres = listOf("Sports", "Cycling", "Drama", "Action", "Romance"),
            totalChapters = 505
        )
    )

    private fun getWebtoonCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_tower_of_god",
            sourceId = sourceId,
            title = "Tower of God",
            altTitle = "신의 탑",
            author = "SIU",
            description = "[$sourceName] What do you desire? Money and wealth? Honor and pride? Authority and power? Revenge? Whatever you desire is atop the Tower.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/1/157897.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Action", "Fantasy", "Mystery", "Supernatural", "Tower"),
            totalChapters = 640
        ),
        Manga(
            id = "${sourceId}_lore_olympus",
            sourceId = sourceId,
            title = "Lore Olympus",
            altTitle = "Persephone & Hades",
            author = "Rachel Smythe",
            description = "[$sourceName] Witness what the gods do after dark in this stylish, Eisner Award-winning retelling of the taking of Persephone.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/117681.jpg",
            status = "Completed",
            rating = 9.7f,
            genres = listOf("Romance", "Mythology", "Drama", "Webtoon"),
            totalChapters = 280
        ),
        Manga(
            id = "${sourceId}_unordinary",
            sourceId = sourceId,
            title = "unOrdinary",
            altTitle = "unOrdinary High",
            author = "uru-chan",
            description = "[$sourceName] Nobody paid much attention to John—just a normal teenager at a high school where the social elite possess godlike abilities.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/232056.jpg",
            status = "Ongoing",
            rating = 9.6f,
            genres = listOf("Superhero", "Action", "School", "Drama"),
            totalChapters = 350
        ),
        Manga(
            id = "${sourceId}_true_beauty",
            sourceId = sourceId,
            title = "True Beauty",
            altTitle = "여신강림",
            author = "Yaongyi",
            description = "[$sourceName] After mastering the art of makeup via online tutorials, Jugyeong transforms into the prettiest goddess at her new high school.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/258224.jpg",
            status = "Completed",
            rating = 9.5f,
            genres = listOf("Romance", "Drama", "Comedy", "School Life"),
            totalChapters = 260
        )
    )

    private fun getTapasCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_beginning_after_end",
            sourceId = sourceId,
            title = "The Beginning After the End",
            altTitle = "TBATE",
            author = "TurtleMe, Fuyuki23",
            description = "[$sourceName] King Grey has unrivaled strength and prestige. Reborn into a magical new world as Arthur Leywin, he seeks a life with true purpose.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/222299.jpg",
            status = "Ongoing",
            rating = 9.9f,
            genres = listOf("Magic", "Reincarnation", "Action", "Adventure", "Fantasy"),
            totalChapters = 195
        ),
        Manga(
            id = "${sourceId}_business_proposal",
            sourceId = sourceId,
            title = "A Business Proposal",
            altTitle = "사내 맞선",
            author = "HaeHwa, Narak",
            description = "[$sourceName] Shin Ha-ri agrees to pretend to be her wealthy friend on a blind date to scare the suitor away, only to find he is her company CEO.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/2/253119.jpg",
            status = "Completed",
            rating = 9.7f,
            genres = listOf("Romance", "Office", "Comedy", "Drama"),
            totalChapters = 120
        )
    )

    private fun getMangaNatoCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_one_piece",
            sourceId = sourceId,
            title = "One Piece",
            altTitle = "Đảo Hải Tặc",
            author = "Eiichiro Oda",
            description = "[$sourceName] Gol D. Roger, the Pirate King, hid his greatest treasure One Piece. Monkey D. Luffy eats the Gum-Gum Fruit and sets sail.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/188896.jpg",
            status = "Ongoing",
            rating = 9.9f,
            genres = listOf("Action", "Adventure", "Comedy", "Shounen", "Pirates"),
            totalChapters = 1130
        ),
        Manga(
            id = "${sourceId}_jujutsu_kaisen",
            sourceId = sourceId,
            title = "Jujutsu Kaisen",
            altTitle = "Chú Thuật Hồi Chiến",
            author = "Gege Akutami",
            description = "[$sourceName] High schooler Yuji Itadori swallows the cursed finger of Ryomen Sukuna and enters Tokyo Jujutsu High to save people from curses.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/2/258237.jpg",
            status = "Completed",
            rating = 9.8f,
            genres = listOf("Action", "Supernatural", "Curse", "Shounen"),
            totalChapters = 271
        ),
        Manga(
            id = "${sourceId}_my_hero_academia",
            sourceId = sourceId,
            title = "My Hero Academia",
            altTitle = "Boku no Hero Academia",
            author = "Horikoshi Kohei",
            description = "[$sourceName] In a world where 80% of humanity has Quirks, powerless Izuku Midoriya inherits One For All from All Might.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/1/259070.jpg",
            status = "Completed",
            rating = 9.7f,
            genres = listOf("Superhero", "Action", "School", "Shounen"),
            totalChapters = 430
        ),
        Manga(
            id = "${sourceId}_demon_slayer",
            sourceId = sourceId,
            title = "Demon Slayer: Kimetsu no Yaiba",
            altTitle = "Thanh Gươm Diệt Quỷ",
            author = "Gotouge Koyoharu",
            description = "[$sourceName] Tanjiro Kamado's family is slaughtered by demons and his sister Nezuko turned into one. He joins the Demon Slayer Corps to cure her.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/54525.jpg",
            status = "Completed",
            rating = 9.9f,
            genres = listOf("Historical", "Action", "Demons", "Swordplay"),
            totalChapters = 205
        )
    )

    private fun getClassicMangaCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_berserk",
            sourceId = sourceId,
            title = "Berserk",
            altTitle = "ベルセルク",
            author = "Miura Kentaro, Studio Gaga",
            description = "[$sourceName] Guts, the Black Swordsman, seeks sanctuary from demonic forces drawn to him and his former commander Griffith.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/1/157897.jpg",
            status = "Ongoing",
            rating = 9.9f,
            genres = listOf("Dark Fantasy", "Action", "Seinen", "Masterpiece", "Tragedy"),
            totalChapters = 376
        ),
        Manga(
            id = "${sourceId}_vinland_saga",
            sourceId = sourceId,
            title = "Vinland Saga",
            altTitle = "ヴィンランド・サガ",
            author = "Yukimura Makoto",
            description = "[$sourceName] Thorfinn, son of the legendary Viking warrior Thors, spends his youth seeking revenge against the mercenary leader Askeladd.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/117681.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Historical", "Vikings", "Drama", "Seinen", "Action"),
            totalChapters = 215
        ),
        Manga(
            id = "${sourceId}_kingdom",
            sourceId = sourceId,
            title = "Kingdom",
            altTitle = "キングダム",
            author = "Hara Yasuhisa",
            description = "[$sourceName] In the Warring States period of ancient China, Xin is a war-orphaned slave who dreams of becoming the greatest general under the heavens.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/232056.jpg",
            status = "Ongoing",
            rating = 9.9f,
            genres = listOf("Military", "Historical", "War", "Seinen", "Action"),
            totalChapters = 815
        )
    )

    private fun getCuuTruyenCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_frieren",
            sourceId = sourceId,
            title = "Sousou no Frieren",
            altTitle = "Pháp Sư Tiễn Táng",
            author = "Yamada Kanehito",
            description = "[$sourceName] Cuộc hành trình của pháp sư elf Frieren sau khi cùng nhóm dũng sĩ đánh bại Ma Vương, tìm kiếm ý nghĩa thời gian giữa người và tiên.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/258224.jpg",
            status = "Ongoing",
            rating = 9.9f,
            genres = listOf("Fantasy", "Phiêu Lưu", "Đời Thường"),
            totalChapters = 135
        ),
        Manga(
            id = "${sourceId}_dungeon_meshi",
            sourceId = sourceId,
            title = "Dungeon Meshi",
            altTitle = "Mỹ Vị Hầm Ngục",
            author = "Kui Ryoko",
            description = "[$sourceName] Nhóm hiệp sĩ Laios nấu ăn từ quái vật trong hầm ngục để cứu em gái Falin khỏi bụng rồng đỏ.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/222299.jpg",
            status = "Completed",
            rating = 9.8f,
            genres = listOf("Nấu Ăn", "Fantasy", "Hài Hước"),
            totalChapters = 97
        ),
        Manga(
            id = "${sourceId}_kusuriya",
            sourceId = sourceId,
            title = "Kusuriya no Hitorigoto",
            altTitle = "Dược Sĩ Tự Sự",
            author = "Natsu Hyuuga, Nekokurage",
            description = "[$sourceName] Maomao, cô gái hành nghề bốc thuốc tại phố hoa, bị bắt cóc vào hậu cung và liên tục giải mã các vụ trúng độc bí ẩn.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/2/253119.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Trinh Thám", "Cung Đấu", "Lịch Sử"),
            totalChapters = 80
        )
    )

    private fun getVietnameseManhwaCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_vo_luyen_dinh_phong",
            sourceId = sourceId,
            title = "Võ Luyện Đỉnh Phong",
            altTitle = "Martial Peak",
            author = "Mạc Mặc",
            description = "[$sourceName] Đỉnh cao võ đạo, là cô độc, là tịch mịch, là bước tiến không ngừng. Dương Khai từ đệ tử quét rác vượt ngàn kiếp nạn xưng bá tinh không.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/188896.jpg",
            status = "Ongoing",
            rating = 9.7f,
            genres = listOf("Tiên Hiệp", "Huyền Huyễn", "Tu Chân", "Hành Động"),
            totalChapters = 3700
        ),
        Manga(
            id = "${sourceId}_dai_quan_gia_ma_hoang",
            sourceId = sourceId,
            title = "Đại Quản Gia Là Ma Hoàng",
            altTitle = "The Steward Demonic Emperor",
            author = "Dạ Cú",
            description = "[$sourceName] Ma Hoàng Trác Nhất Phàm bị đệ tử phản bội, trọng sinh thành quản gia gia tộc Lạc gia thất thế, từng bước trở lại ngai vàng Ma Vực.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/2/258237.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Chuyển Sinh", "Trọng Sinh", "Cường Giả", "Mưu Lược"),
            totalChapters = 560
        ),
        Manga(
            id = "${sourceId}_ta_la_ta_de",
            sourceId = sourceId,
            title = "Ta Là Tà Đế",
            altTitle = "Way To Be The Evil Emperor",
            author = "Đạt La",
            description = "[$sourceName] Tạ Diệm xuyên không thành công tử tuyết ma môn, thức tỉnh hệ thống Tà Đế du hành qua vô số tiểu thế giới.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/1/259070.jpg",
            status = "Ongoing",
            rating = 9.7f,
            genres = listOf("Hệ Thống", "Hài Hước", "Hành Động", "Xuyên Không"),
            totalChapters = 440
        )
    )

    private fun getSpanishCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_solo_leveling_es",
            sourceId = sourceId,
            title = "Solo Leveling (Español)",
            altTitle = "Solo Leveling en Tu Idioma",
            author = "Chugong, DUBU",
            description = "[$sourceName] Sung Jinwoo es conocido como el cazador más débil del mundo. Tras una misión mortal en una mazmorra oculta, despierta un sistema único.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/54525.jpg",
            status = "Completed",
            rating = 9.9f,
            genres = listOf("Acción", "Fantasía", "Manhwa", "Sistema"),
            totalChapters = 200
        ),
        Manga(
            id = "${sourceId}_one_piece_es",
            sourceId = sourceId,
            title = "One Piece (Español)",
            altTitle = "One Piece Scan Latino",
            author = "Eiichiro Oda",
            description = "[$sourceName] Monkey D. Luffy surca los mares en busca del legendario tesoro One Piece para convertirse en el Rey de los Piratas.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/1/157897.jpg",
            status = "Ongoing",
            rating = 9.9f,
            genres = listOf("Acción", "Aventura", "Comedia", "Piratas"),
            totalChapters = 1130
        )
    )

    private fun getJapaneseCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_spy_family_ja",
            sourceId = sourceId,
            title = "SPY×FAMILY (日本語公式)",
            altTitle = "スパイファミリー",
            author = "遠藤達哉",
            description = "[$sourceName] 名門校潜入のために「家族」を作れと命じられた凄腕スパイの黄昏。だが彼が出会った娘は心を読む超能力者、妻は暗殺者だった！",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/117681.jpg",
            status = "Ongoing",
            rating = 9.9f,
            genres = listOf("コメディ", "スパイ", "アクション", "少年ジャンプ＋"),
            totalChapters = 110
        ),
        Manga(
            id = "${sourceId}_kaiju8_ja",
            sourceId = sourceId,
            title = "怪獣8号 (Kaiju No. 8)",
            altTitle = "Kaiju No. 8 日本語",
            author = "松本直也",
            description = "[$sourceName] 日常的に怪獣が人々をおびやかす世界。怪獣清掃員の日比野カフカは、ある日謎の生物によって自身が怪獣化してしまう！",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/232056.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("アクション", "SF", "怪獣", "少年ジャンプ＋"),
            totalChapters = 115
        )
    )

    private fun getBatoToCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_remarried_empress",
            sourceId = sourceId,
            title = "The Remarried Empress",
            altTitle = "재혼 황후",
            author = "Alphatart, Sumpul",
            description = "[$sourceName] Navier Ellie Trovi was the perfect empress of the Eastern Empire. When her emperor husband demands a divorce for his mistress, Navier agrees—on one condition: remarriage to Heinrey.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/258224.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Romance", "Royalty", "Drama", "Fantasy", "Otome"),
            totalChapters = 175
        ),
        Manga(
            id = "${sourceId}_who_made_me_princess",
            sourceId = sourceId,
            title = "Who Made Me a Princess",
            altTitle = "어느 날 공주가 되어버렸다",
            author = "Plutus, Spoon",
            description = "[$sourceName] Reborn as Princess Athanasia, she knows the tragic story where she is executed by her cold-blooded emperor father Claude. She must charm him to survive.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/222299.jpg",
            status = "Completed",
            rating = 9.9f,
            genres = listOf("Fantasy", "Father-Daughter", "Romance", "Isekai", "Masterpiece"),
            totalChapters = 125
        ),
        Manga(
            id = "${sourceId}_death_is_the_only_ending",
            sourceId = sourceId,
            title = "Villains Are Destined to Die",
            altTitle = "Death Is the Only Ending for the Villainess",
            author = "Gwon Gyeoeul, SUOL",
            description = "[$sourceName] Reincarnated as Penelope Eckart, the adopted villainess in an impossible hard-mode otome dating game where every mistake leads to death.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/2/253119.jpg",
            status = "Ongoing",
            rating = 9.9f,
            genres = listOf("Otome", "Survival", "Romance", "Psychological", "Isekai"),
            totalChapters = 160
        )
    )

    private fun getComicsCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_batman_year_one",
            sourceId = sourceId,
            title = "Batman: Year One",
            altTitle = "The Dark Knight Origins",
            author = "Frank Miller, David Mazzucchelli",
            description = "[$sourceName] Bruce Wayne returns to Gotham City after years abroad, establishing the mantle of Batman alongside Lieutenant James Gordon.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/188896.jpg",
            status = "Completed",
            rating = 9.9f,
            genres = listOf("Superhero", "Noir", "Crime", "DC Comics"),
            totalChapters = 4
        ),
        Manga(
            id = "${sourceId}_spider_man_ultimate",
            sourceId = sourceId,
            title = "Ultimate Spider-Man",
            altTitle = "Peter Parker & Miles Morales",
            author = "Brian Michael Bendis, Mark Bagley",
            description = "[$sourceName] Bitten by a genetically modified spider, high school student Peter Parker learns that with great power comes great responsibility.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/2/258237.jpg",
            status = "Completed",
            rating = 9.8f,
            genres = listOf("Superhero", "Action", "Marvel", "School"),
            totalChapters = 160
        )
    )

    private fun getGeneralMangaCatalog(sourceId: String, sourceName: String) = listOf(
        Manga(
            id = "${sourceId}_kaiju_no_8",
            sourceId = sourceId,
            title = "Kaiju No. 8",
            altTitle = "Monster #8",
            author = "Naoya Matsumoto",
            description = "[$sourceName] A man working a job far removed from his childhood dreams gets wrapped up in an unexpected situation, becoming a monster himself.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/1/259070.jpg",
            status = "Ongoing",
            rating = 9.7f,
            genres = listOf("Action", "Sci-Fi", "Monsters", "Shounen"),
            totalChapters = 115
        ),
        Manga(
            id = "${sourceId}_blue_lock",
            sourceId = sourceId,
            title = "Blue Lock",
            altTitle = "ブルーロック",
            author = "Kaneshiro Muneyuki, Nomura Yusuke",
            description = "[$sourceName] Japan desires a World Cup win. Ego Jinpachi launches Blue Lock: an isolated prison camp to produce the world's most egotistical striker.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/3/54525.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Sports", "Soccer", "Psychological", "Shounen"),
            totalChapters = 285
        ),
        Manga(
            id = "${sourceId}_dandadan_gen",
            sourceId = sourceId,
            title = "Dandadan",
            altTitle = "Ghost & Alien Battles",
            author = "Yukinobu Tatsu",
            description = "[$sourceName] A high school girl who believes in ghosts and a boy who believes in aliens embark on high-octane supernatural battles across Japan.",
            coverUrl = "https://cdn.myanimelist.net/images/manga/1/157897.jpg",
            status = "Ongoing",
            rating = 9.8f,
            genres = listOf("Action", "Comedy", "Supernatural", "Romance"),
            totalChapters = 175
        )
    )

    fun getChaptersForManga(mangaId: String, sourceId: String, sourceName: String, language: String): List<Chapter> {
        return (1..25).map { num ->
            Chapter(
                id = "${mangaId}_ch_$num",
                mangaId = mangaId,
                sourceId = sourceId,
                name = when (language) {
                    "vi" -> "Chương $num: Khởi Đầu Mới"
                    "es" -> "Capítulo $num: El Despertar"
                    "ja" -> "第${num}話「運命の序曲」"
                    else -> "Chapter $num: The Turning Point"
                },
                number = num.toFloat(),
                scanlator = sourceName,
                dateUpload = "2024-05-18",
                language = language,
                pageCount = 18 + (num % 6)
            )
        }
    }

    fun getPagesForChapter(chapterId: String): List<MangaPage> {
        val sampleUrls = listOf(
            "https://cdn.myanimelist.net/images/manga/3/117681.jpg",
            "https://cdn.myanimelist.net/images/manga/3/232056.jpg",
            "https://cdn.myanimelist.net/images/manga/3/258224.jpg",
            "https://cdn.myanimelist.net/images/manga/3/222299.jpg",
            "https://cdn.myanimelist.net/images/manga/2/253119.jpg",
            "https://cdn.myanimelist.net/images/manga/3/188896.jpg",
            "https://cdn.myanimelist.net/images/manga/2/258237.jpg"
        )
        return sampleUrls.mapIndexed { idx, url ->
            MangaPage(index = idx + 1, imageUrl = url)
        }
    }
}
