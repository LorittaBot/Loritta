package net.perfectdreams.loritta.loricoolcards.generator

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import net.perfectdreams.loritta.common.loricoolcards.CardRarity
import net.perfectdreams.loritta.morenitta.loricoolcards.StickerAlbumTemplate
import java.io.File

data class TemplatingSlots(
    val imageFileName: String,
    val slots: MutableList<StickerAlbumTemplate.StickerSlot>
)

fun main() {
    val stickersToBePlaced = (1..510).toMutableList()

    fun loadTemplatingSlots(fileName: String): TemplatingSlots {
        // Load templating slots from file
        val tentativeTemplatingSlots = mutableListOf<StickerAlbumTemplate.StickerSlot>()

        var isFirstLine = true
        var imageFileName: String? = null

        File("D:\\Pictures\\Loritta\\LoriCoolCards\\pages\\$fileName.psd.txt")
            .readLines()
            .forEach {
                if (isFirstLine) {
                    isFirstLine = false

                    imageFileName = it
                } else {
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
            }

        return TemplatingSlots(
            imageFileName!!,
            tentativeTemplatingSlots
                .sortedBy {
                    it.stickerId.removePrefix("[STICKER] ")
                        .trim()
                        .toInt()
                }.map { it.copy(stickerId = "TEMPLATE") }.toMutableList()
        )
    }

    // TODO: It is "tentativeTemplatingSlots" because we need to sort the stickers!
    //  There are two ways to sort them:
    //  Based off the layer name
    //  Or just sort it based on the sticker's xy position
    val templateSlots = mapOf(
        1 to loadTemplatingSlots("new/v3/new_album_first_page"),
        3 to loadTemplatingSlots("new/v3/new_album_second_page"),
        // 5 to loadTemplatingSlots("combo_lori_sleepy"),
        5 to loadTemplatingSlots("new/v3/new_album_sparkly"), // full page
        7 to loadTemplatingSlots("new/v3/new_album_lori_beach"), // big
        9 to loadTemplatingSlots("new/v3/new_album_loritta_and_the_dreamers"), // small
        11 to loadTemplatingSlots("new/v3/new_album_lori_and_wumpus"), // small
        13 to loadTemplatingSlots("new/v3/new_album_lori_hey-hey-my-my-yo-yo"), // small
        15 to loadTemplatingSlots("new/v3/new_album_herry"), // full page
        17 to loadTemplatingSlots("new/v3/new_album_lori_water"), // big
        19 to loadTemplatingSlots("new/v3/new_album_lori_deitada"), // small
        21 to loadTemplatingSlots("new/v3/new_album_gabriela_easel"), // small
        23 to loadTemplatingSlots("new/v3/new_album_lori_pantufa_gabi"), // small
        25 to loadTemplatingSlots("new/v3/new_album_legoshi"), // full page
        27 to loadTemplatingSlots("new/v3/new_album_lori_stars_yafyr"), // big
        29 to loadTemplatingSlots("new/v3/new_album_lori_sleepy"), // small
        31 to loadTemplatingSlots("new/v3/new_album_fofoca"), // small
        33 to loadTemplatingSlots("new/v3/new_album_lori_running_sonhos"), // small
        35 to loadTemplatingSlots("new/v3/new_album_sao_joao"), // full page
        37 to loadTemplatingSlots("new/v3/new_album_lori_cool_pose"), // big
        39 to loadTemplatingSlots("new/v3/new_album_lori_you_bring_light_in"), // small
        41 to loadTemplatingSlots("new/v3/new_album_lori_code"), // small
        43 to loadTemplatingSlots("new/v3/new_album_sips"), // small
        45 to loadTemplatingSlots("new/v3/new_album_lori_mari_figurittas"), // full page

        -1 to loadTemplatingSlots("new/v3/new_album_generic_page"),

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

    println("Filled slots: ${templateSlots.entries.filter { it.key > 0 }.sumOf { it.value.slots.size }}")
    println("Slots missing: ${stickersToBePlaced.size - templateSlots.entries.filter { it.key > 0 }.sumOf { it.value.slots.size }}")
    println("How many pages are missing? (considering 32 stickers per page): ${(stickersToBePlaced.size - templateSlots.entries.filter { it.key > 0 }.sumOf { it.value.slots.size }) / 32}")
    println("How many pages are missing? (considering 28 stickers per page): ${(stickersToBePlaced.size - templateSlots.entries.filter { it.key > 0 }.sumOf { it.value.slots.size }) / 28}")
    println("How many pages are missing? (considering 24 stickers per page): ${(stickersToBePlaced.size - templateSlots.entries.filter { it.key > 0 }.sumOf { it.value.slots.size }) / 24}")
    println("How many pages are missing? (considering 20 stickers per page): ${(stickersToBePlaced.size - templateSlots.entries.filter { it.key > 0 }.sumOf { it.value.slots.size }) / 20}")
    println("How many pages are missing? (considering 16 stickers per page): ${(stickersToBePlaced.size - templateSlots.entries.filter { it.key > 0 }.sumOf { it.value.slots.size }) / 16}")

    var currentPage = 1
    var currentPageSlots = (templateSlots[currentPage] ?: templateSlots[-1]!!).slots.toMutableList()
    val pages = mutableListOf<StickerAlbumTemplate.AlbumComboPage>()

    while (stickersToBePlaced.isNotEmpty()) {
        val stickerToBePlaced = stickersToBePlaced.first()
        val templateSlot = currentPageSlots.indexOfFirst { it.stickerId == "TEMPLATE" }

        if (templateSlot == -1) {
            // Needs to create new page!
            val pageBackdropImageFileName = (templateSlots[currentPage] ?: templateSlots[-1]!!).imageFileName
            val pageBackdropUrl = "https://stuff.loritta.website/loricoolcards/production/v3/pages/$pageBackdropImageFileName"

            pages.add(
                StickerAlbumTemplate.AlbumComboPage(
                    currentPage++,
                    currentPage++,
                    pageBackdropUrl,
                    currentPageSlots
                )
            )
            currentPageSlots = (templateSlots[currentPage] ?: templateSlots[-1]!!).slots.toMutableList()
            continue
        }

        currentPageSlots[templateSlot] = currentPageSlots[templateSlot].copy(stickerId = "#${stickerToBePlaced.toString().padStart(4, '0')}")
        stickersToBePlaced.removeFirst()
    }

    val nonTemplatingSlots = currentPageSlots.filter { it.stickerId != "TEMPLATE" }

    // Add not finished page
    if (nonTemplatingSlots.isNotEmpty()) {
        val pageBackdropImageFileName = (templateSlots[currentPage] ?: templateSlots[-1]!!).imageFileName
        val pageBackdropUrl = "https://stuff.loritta.website/loricoolcards/production/v3/pages/$pageBackdropImageFileName"

        pages.add(
            StickerAlbumTemplate.AlbumComboPage(
                currentPage++,
                currentPage++,
                pageBackdropUrl,
                nonTemplatingSlots
            )
        )
    }

    println(
        "UPDATE loricoolcardsevents SET template = '${Json.encodeToString(
            StickerAlbumTemplate(
                stickerPackImageUrl = "https://stuff.loritta.website/loricoolcards/production/v1/buying-booster-pack.gif",
                unknownStickerImageUrl = "https://stuff.loritta.website/loricoolcards/production/v1/sticker-unknownsticker-animated.gif",
                sonhosPrice = 5_000,
                sonhosReward = 2_000_000,
                stickersInPack = 5,
                stickerProbabilityWeights = mapOf(
                    CardRarity.COMMON to 22.0,
                    CardRarity.UNCOMMON to 20.0,
                    CardRarity.RARE to 18.0,
                    CardRarity.EPIC to 16.0,
                    CardRarity.LEGENDARY to 13.0,
                    CardRarity.MYTHIC to 11.0,
                ),
                pages = pages
            )
        )}' WHERE id = 9;"
    )
}