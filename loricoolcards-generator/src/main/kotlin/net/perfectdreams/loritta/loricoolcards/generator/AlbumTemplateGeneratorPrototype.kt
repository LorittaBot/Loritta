package net.perfectdreams.loritta.loricoolcards.generator

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.common.loricoolcards.CardRarity
import net.perfectdreams.loritta.morenitta.loricoolcards.StickerAlbumTemplate
import java.io.File

fun main() {
    val stickersToBePlaced = (1..510).toMutableList()

    fun loadTemplatingSlots(fileName: String): MutableList<StickerAlbumTemplate.StickerSlot> {
        // Load templating slots from file
        val tentativeTemplatingSlots = mutableListOf<StickerAlbumTemplate.StickerSlot>()

        File("D:\\Pictures\\Loritta\\LoriCoolCards\\pages\\$fileName.psd.txt")
            .readLines()
            .forEach {
                val (layerName, topLeftXString, topLeftYString, bottomRightXString, bottomRightYString) = it.split(";")
                val topLeftX = topLeftXString.toInt()
                val topLeftY = topLeftYString.toInt()
                val bottomRightX = bottomRightXString.toInt()
                val bottomRightY = bottomRightYString.toInt()

                tentativeTemplatingSlots.add(
                    StickerAlbumTemplate.StickerSlot(
                        layerName,
                        topLeftX,
                        topLeftY,
                        bottomRightX - topLeftX,
                        bottomRightY - topLeftY,
                        8,
                        32
                    )
                )
            }

        return tentativeTemplatingSlots.sortedBy { it.stickerId.removePrefix("[STICKER] ")
            .trim()
            .toInt()
        }.map { it.copy(stickerId = "TEMPLATE") } .toMutableList()
    }

    // TODO: It is "tentativeTemplatingSlots" because we need to sort the stickers!
    //  There are two ways to sort them:
    //  Based off the layer name
    //  Or just sort it based on the sticker's xy position
    val templateSlots = mapOf(
        1 to loadTemplatingSlots("new/new_album_first_page"),
        3 to loadTemplatingSlots("new/new_album_second_page"),
        // 5 to loadTemplatingSlots("combo_lori_sleepy"),
        5 to loadTemplatingSlots("new/new_album_lori_stars_yafyr"),
        7 to loadTemplatingSlots("new/new_album_lori_sleepy"),
        9 to loadTemplatingSlots("new/new_album_lori_pantufa_gabi"),
        11 to loadTemplatingSlots("new/new_album_lori_hey-hey-my-my-yo-yo"),
        13 to loadTemplatingSlots("new/new_album_lori_and_wumpus"),
        15 to loadTemplatingSlots("new/new_album_lori_running_sonhos"),
        17 to loadTemplatingSlots("new/new_album_fofoca"),
        19 to loadTemplatingSlots("new/new_album_gabriela_easel"),
        21 to loadTemplatingSlots("new/new_album_lori_deitada"),
        23 to loadTemplatingSlots("new/new_album_loritta_and_the_dreamers"),
        25 to loadTemplatingSlots("new/new_album_legoshi"),
        27 to loadTemplatingSlots("new/new_album_lori_cool_pose"),
        29 to loadTemplatingSlots("new/new_album_lori_you_bring_light_in"),
        31 to loadTemplatingSlots("new/new_album_lori_beach"),
        33 to loadTemplatingSlots("new/new_album_lori_code"),
        35 to loadTemplatingSlots("new/new_album_lori_water"),
        37 to loadTemplatingSlots("new/new_album_sips"),
        39  to loadTemplatingSlots("new/new_album_sparkly"),

        -1 to loadTemplatingSlots("new/new_album_generic_page"),

        /* -1 to listOf(
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                16,
                84,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                205,
                84,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                394,
                84,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                583,
                84,
                173,
                246,
                8,
                32
            ),

            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                16,
                346,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                205,
                346,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                394,
                346,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                583,
                346,
                173,
                246,
                8,
                32
            ),

            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                16,
                608,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                205,
                608,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                394,
                608,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                583,
                608,
                173,
                246,
                8,
                32
            ),

            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                16,
                870,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                205,
                870,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                394,
                870,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                583,
                870,
                173,
                246,
                8,
                32
            ),

            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 583 - 173,
                84,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 394 - 173,
                84,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 205 - 173,
                84,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 16 - 173,
                84,
                173,
                246,
                8,
                32
            ),

            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 583 - 173,
                346,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 394 - 173,
                346,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 205 - 173,
                346,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 16 - 173,
                346,
                173,
                246,
                8,
                32
            ),

            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 583 - 173,
                608,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 394 - 173,
                608,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 205 - 173,
                608,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 16 - 173,
                608,
                173,
                246,
                8,
                32
            ),

            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 583 - 173,
                870,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 394 - 173,
                870,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 205 - 173,
                870,
                173,
                246,
                8,
                32
            ),
            StickerAlbumTemplate.StickerSlot(
                "TEMPLATE",
                1600 - 16 - 173,
                870,
                173,
                246,
                8,
                32
            )
        ) */
    )

    println("Filled slots: ${templateSlots.entries.filter { it.key > 0 }.sumOf { it.value.size }}")
    println("Slots missing: ${stickersToBePlaced.size - templateSlots.entries.filter { it.key > 0 }.sumOf { it.value.size }}")
    println("How many pages are missing? (considering 32 stickers per page): ${(stickersToBePlaced.size - templateSlots.entries.filter { it.key > 0 }.sumOf { it.value.size }) / 32}")
    println("How many pages are missing? (considering 28 stickers per page): ${(stickersToBePlaced.size - templateSlots.entries.filter { it.key > 0 }.sumOf { it.value.size }) / 28}")
    println("How many pages are missing? (considering 24 stickers per page): ${(stickersToBePlaced.size - templateSlots.entries.filter { it.key > 0 }.sumOf { it.value.size }) / 24}")
    println("How many pages are missing? (considering 20 stickers per page): ${(stickersToBePlaced.size - templateSlots.entries.filter { it.key > 0 }.sumOf { it.value.size }) / 20}")
    println("How many pages are missing? (considering 16 stickers per page): ${(stickersToBePlaced.size - templateSlots.entries.filter { it.key > 0 }.sumOf { it.value.size }) / 16}")

    var currentPage = 1
    var currentPageSlots = (templateSlots[currentPage] ?: templateSlots[-1]!!).toMutableList()
    val pages = mutableListOf<StickerAlbumTemplate.AlbumComboPage>()

    while (stickersToBePlaced.isNotEmpty()) {
        val stickerToBePlaced = stickersToBePlaced.first()
        val templateSlot = currentPageSlots.indexOfFirst { it.stickerId == "TEMPLATE" }

        if (templateSlot == -1) {
            // Needs to create new page!
            val pageBackdropUrl = when (currentPage) {
                1 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_first_page.psd.png"
                3 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_second_page.psd.png"
                5 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_lori_stars_yafyr.psd.png"
                7 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_lori_sleepy.psd.png"
                9 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_lori_pantufa_gabi.psd.png"
                11 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_lori_hey-hey-my-my-yo-yo.psd.png"
                13 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_lori_and_wumpus.psd.png"
                15 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_lori_running_sonhos.psd.png"
                17 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_fofoca.psd.png"
                19 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_gabriela_easel.psd.png"
                21 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_lori_deitada.psd.png"
                23 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_loritta_and_the_dreamers.psd.png"
                25 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_legoshi.psd.png"
                27 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_lori_cool_pose.psd.png"
                29 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_lori_you_bring_light_in.psd.png"
                31 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_lori_beach.psd.png"
                33 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_lori_code.psd.png"
                35 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_lori_water.psd.png"
                37 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_sips.psd.png"
                39 -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_sparkly.psd.png"
                else -> "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_generic_page.psd.png"
            }
            pages.add(
                StickerAlbumTemplate.AlbumComboPage(
                    currentPage++,
                    currentPage++,
                    pageBackdropUrl,
                    currentPageSlots
                )
            )
            currentPageSlots = (templateSlots[currentPage] ?: templateSlots[-1]!!).toMutableList()
            continue
        }

        currentPageSlots[templateSlot] = currentPageSlots[templateSlot].copy(stickerId = "#${stickerToBePlaced.toString().padStart(4, '0')}")
        stickersToBePlaced.removeFirst()
    }

    val nonTemplatingSlots = currentPageSlots.filter { it.stickerId != "TEMPLATE" }

    // Add not finished page
    if (nonTemplatingSlots.isNotEmpty()) {
        pages.add(
            StickerAlbumTemplate.AlbumComboPage(
                currentPage++,
                currentPage++,
                "file:///D:/Pictures/Loritta/LoriCoolCards/pages/new/new_album_generic_page.psd.png",
                nonTemplatingSlots
            )
        )
    }

    println(
        "UPDATE loricoolcardsevents SET template = '${Json.encodeToString(
            StickerAlbumTemplate(
                stickerPackImageUrl = "https://cdn.discordapp.com/attachments/1169984595131904010/1230605647691911241/buying_package_v3_gifsicle_test_v4.gif?ex=6633edd1&is=662178d1&hm=b38727bac519a9b50fc6e0f8a8e9704511d7da931316bea7d7e62d20ad62213a&",
                unknownStickerImageUrl = "https://cdn.discordapp.com/attachments/1169984595131904010/1230605647691911241/buying_package_v3_gifsicle_test_v4.gif?ex=6633edd1&is=662178d1&hm=b38727bac519a9b50fc6e0f8a8e9704511d7da931316bea7d7e62d20ad62213a&",
                sonhosPrice = 5_000,
                sonhosReward = 5_000,
                stickersInPack = 5,
                stickerProbabilityWeights = mapOf(
                    CardRarity.COMMON to 1.0,
                    CardRarity.UNCOMMON to 1.0,
                    CardRarity.RARE to 1.0,
                    CardRarity.EPIC to 1.0,
                    CardRarity.LEGENDARY to 1.0,
                    CardRarity.MYTHIC to 1.0,
                ),
                pages = pages
            )
        )}';"
    )
}