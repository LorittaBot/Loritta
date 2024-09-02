package net.perfectdreams.loritta.morenitta.loricoolcards

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.loricoolcards.CardRarity

@Serializable
data class StickerAlbumTemplate(
    /**
     * The sticker pack image URL used in [LoriCoolCardsBuyStickersExecutor]
     */
    val stickerPackImageUrl: String,
    /**
     * Unknown sticker image URL
     */
    val unknownStickerImageUrl: String,
    /**
     * The price of the sticker pack
     */
    val sonhosPrice: Long,
    /**
     * The amount of stickers that comes in the sticker pack
     */
    val stickersInPack: Int,
    /**
     * Sonhos Reward when someone completes the album
     */
    val sonhosReward: Long,
    /**
     * The weights (probability) of each sticker
     */
    val stickerProbabilityWeights: Map<CardRarity, Double>,
    /**
     * How many booster packs the user must buy before being able to trade
     */
    val minimumBoosterPacksToTrade: Int = 0,
    /**
     * The pages of the album
     */
    val pages: List<AlbumComboPage>,
) {
    @Serializable
    data class AlbumComboPage(
        val pageLeft: Int,
        val pageRight: Int,
        val pageBackdropUrl: String, // The backdrop of the page
        val slots: List<StickerSlot>
    )

    @Serializable
    data class StickerSlot(
        /**
         * Uses the fancy sticker ID, not the database ID!
         */
        val stickerId: String,
        /**
         * X coordinate in the image
         */
        val x: Int,
        /**
         * Y coordinate in the image
         */
        val y: Int,
        val width: Int,
        val height: Int,
        val outline: Int,
        val stickerIdFontSize: Int
    )

    /**
     * Finds the [AlbumComboPage] that contains the [fancyStickerId] (example: #0001)
     */
    fun getAlbumComboPageThatHasSticker(fancyStickerId: String): AlbumComboPage? {
        return pages.firstOrNull { page ->
            page.slots.any { it.stickerId == fancyStickerId }
        }
    }

    /**
     * Finds the page that contains the [fancyStickerId] (example: #0001)
     */
    fun getAlbumPageThatHasSticker(fancyStickerId: String): Int? {
        val albumComboPage = getAlbumComboPageThatHasSticker(fancyStickerId) ?: return null

        // TODO: This is a bit hacky because we are hardcoding the album width
        //  The *best* solution would be splitting up the slots into two different pages
        //  But tbh it doesn't really matter let's be honest, this would only break if we change the album width size and even then we can just store the page width on the template itself
        val slot = albumComboPage.slots.first {
            it.stickerId == fancyStickerId
        }

        val albumWidthDividedBy2 = 1600 / 2

        return if (slot.x > albumWidthDividedBy2)
            albumComboPage.pageRight
        else
            albumComboPage.pageLeft
    }

    /**
     * Finds the [AlbumComboPage] that matches the [page], searching for any [AlbumComboPage.pageLeft] or [AlbumComboPage.pageRight] that matches
     */
    fun getAlbumComboPageByPage(page: Int): AlbumComboPage? {
        return pages.firstOrNull {
            it.pageLeft == page || it.pageRight == page
        }
    }
}