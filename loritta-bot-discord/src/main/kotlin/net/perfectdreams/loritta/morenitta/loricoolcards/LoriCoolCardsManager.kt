package net.perfectdreams.loritta.morenitta.loricoolcards

import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import net.perfectdreams.loritta.cinnamon.discord.utils.images.InterpolationType
import net.perfectdreams.loritta.cinnamon.discord.utils.images.getResizedInstance
import net.perfectdreams.loritta.cinnamon.discord.utils.images.readImageFromResources
import net.perfectdreams.loritta.cinnamon.discord.utils.toJavaColor
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsEventCards
import net.perfectdreams.loritta.common.loricoolcards.CardRarity
import net.perfectdreams.loritta.common.utils.LorittaImage
import net.perfectdreams.loritta.common.utils.extensions.enableFontAntiAliasing
import net.perfectdreams.loritta.common.utils.math.Easings
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.GraphicsFonts
import net.perfectdreams.loritta.morenitta.utils.ImageUtils
import net.perfectdreams.loritta.morenitta.utils.images.MultiplyComposite
import net.perfectdreams.loritta.morenitta.utils.images.gifs.AnimatedGifEncoder
import org.jetbrains.exposed.sql.ResultRow
import java.awt.*
import java.awt.font.GlyphVector
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.awt.image.IndexColorModel
import java.io.ByteArrayOutputStream
import java.io.File
import java.net.URI
import java.net.URL
import java.util.*
import javax.imageio.ImageIO
import kotlin.concurrent.thread


class LoriCoolCardsManager(val graphicsFonts: GraphicsFonts) {
    companion object {
        private val FRONT_FACING_CARD_BACKGROUND_BLUR_KERNEL = ImageUtils.generateGaussianBlurKernel(8)
    }

    /**
     * The palette used for the "Sticker received GIF" and other GIFs related to stickers
     *
     * Using a fixed palette like this is WAY FASTER than using NeuQuant or anything like that, and the results are... actually pretty nice!?!
     *
     * The palette is from Sonic Robo Blast v2.2!
     *
     * https://lospec.com/palette-list/sonic-robo-blast-2-v22
     */
    private val srb2Palette: List<Color> = runBlocking { ImageUtils.convertToPaletteList(readImageFromResources("/loricoolcards/srb2_palette.png")) }

    /**
     * The color table used on the "Sticker received GIF" and other GIFs related to stickers
     */
    private val stickerReceivedColorTab = srb2Palette.map {
        listOf(
            it.red.toByte(),
            it.green.toByte(),
            it.blue.toByte()
        )
    }.flatten().toByteArray()

    private val sparkles1 = runBlocking { readImageFromResources("/loricoolcards/sparkles_1.png") }
    private val sparkles2 = runBlocking { readImageFromResources("/loricoolcards/sparkles_2.png") }
    private val sparkles3 = runBlocking { readImageFromResources("/loricoolcards/sparkles_3.png") }
    private val starPattern = runBlocking { readImageFromResources("/loricoolcards/star_pattern.png") }
    private val scissorsPattern = runBlocking { readImageFromResources("/loricoolcards/scissors_pattern.png") }
    private val stickerBase = runBlocking { readImageFromResources("/loricoolcards/sticker_base.png") }
    private val glareOverlay = runBlocking { readImageFromResources("/loricoolcards/glare_overlay.png") }
    private val badgeFallbackOverlay = runBlocking { readImageFromResources("/loricoolcards/badge_fallback_overlay.png") }
    private val legendaryOverlay = runBlocking { readImageFromResources("/loricoolcards/legendary_overlay.png") }

    /**
     * Generates the album base in BGR format, as in, the first frame used for animations and other commands
     */
    fun generateAlbumBase(
        albumTemplate: StickerAlbumTemplate,
        alreadyStickedCards: List<ResultRow>,
        albumPage: StickerAlbumTemplate.AlbumComboPage,
        targetSize: Float
    ): BufferedImage {
        fun remapPixels(input: Int): Int {
            return (input * targetSize).toInt()
        }

        // Download the page template
        val imageBeforeResizingAndConversions = ImageIO.read(URI(albumPage.pageBackdropUrl).toURL())
        val image = ImageUtils.copyToBufferedImageBGR(imageBeforeResizingAndConversions.getResizedInstance(remapPixels(imageBeforeResizingAndConversions.width), remapPixels(imageBeforeResizingAndConversions.height), InterpolationType.BILINEAR))

        // Create base
        run {
            val graphics = image.createGraphics()
            graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
            )
            graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            )

            // Page numbers
            graphics.color = Color.WHITE
            graphics.font = graphicsFonts.latoBlack.deriveFont(remapPixels(40).toFloat())
            ImageUtils.drawCenteredStringOutlined(
                graphics,
                albumPage.pageLeft.toString(),
                Rectangle(
                    remapPixels(0),
                    remapPixels(1146),
                    remapPixels(80),
                    remapPixels(54),
                ),
                graphics.font
            )

            ImageUtils.drawCenteredStringOutlined(
                graphics,
                albumPage.pageRight.toString(),
                Rectangle(
                    remapPixels(1600 - 80),
                    remapPixels(1146),
                    remapPixels(80),
                    remapPixels(54),
                ),
                graphics.font
            )

            for (stickerSlot in albumPage.slots) {
                val stickerInfo = alreadyStickedCards.firstOrNull { resultRow ->
                    resultRow[LoriCoolCardsEventCards.fancyCardId] == stickerSlot.stickerId
                }

                if (stickerInfo != null) {
                    // TODO: Preload the sticker images?
                    val alreadyStickedCard = ImageIO.read(URL(stickerInfo[LoriCoolCardsEventCards.cardFrontImageUrl]))
                    graphics.drawImage(
                        alreadyStickedCard,
                        remapPixels(stickerSlot.x),
                        remapPixels(stickerSlot.y),
                        remapPixels(stickerSlot.width),
                        remapPixels(stickerSlot.height),
                        null
                    )
                } else {
                    // Not sticked yet
                    graphics.color = Color(0, 0, 0, 90)
                    graphics.fillRect(
                        remapPixels(stickerSlot.x),
                        remapPixels(stickerSlot.y),
                        remapPixels(stickerSlot.width),
                        remapPixels(stickerSlot.height),
                    )

                    graphics.color = Color(0, 0, 0, 60)
                    repeat(stickerSlot.outline) {
                        graphics.drawRect(
                            remapPixels((stickerSlot.x) + it),
                            remapPixels((stickerSlot.y) + it),
                            remapPixels((stickerSlot.width) - (it * 2)),
                            remapPixels((stickerSlot.height) - (it * 2))
                        )
                    }

                    // Change the text color based on the background color
                    // This is not perfect (technically we need to check the entire background, not just one pixel), but...
                    // https://stackoverflow.com/a/3943023/7271796
                    val backgroundColor = Color(image.getRGB(remapPixels(stickerSlot.x), remapPixels(stickerSlot.y)))

                    graphics.color = if (backgroundColor.red * 0.299 + backgroundColor.green * 0.587 + backgroundColor.blue * 0.114 > 186) {
                        Color.BLACK
                    } else {
                        Color.WHITE
                    }

                    graphics.font = graphicsFonts.latoBlack.deriveFont(remapPixels(stickerSlot.stickerIdFontSize).toFloat())
                    ImageUtils.drawCenteredString(
                        graphics,
                        stickerSlot.stickerId,
                        Rectangle(
                            remapPixels(stickerSlot.x),
                            remapPixels(stickerSlot.y),
                            remapPixels(stickerSlot.width),
                            remapPixels(stickerSlot.height),
                        ),
                        graphics.font
                    )
                }
            }
        }

        return image
    }

    /**
     * Generates the album base in BGR format, as in, the first frame used for animations and other commands
     */
    fun generateAlbumBaseMT(
        albumTemplate: StickerAlbumTemplate,
        alreadyStickedCards: List<ResultRow>,
        albumPage: StickerAlbumTemplate.AlbumComboPage,
        targetSize: Float
    ): BufferedImage {
        fun remapPixels(input: Int): Int {
            return (input * targetSize).toInt()
        }

        // Download the page template
        val imageBeforeResizingAndConversions = ImageIO.read(URI(albumPage.pageBackdropUrl).toURL())
        val image = ImageUtils.copyToBufferedImageBGR(imageBeforeResizingAndConversions.getResizedInstance(remapPixels(imageBeforeResizingAndConversions.width), remapPixels(imageBeforeResizingAndConversions.height), InterpolationType.BILINEAR))

        // Create base
        run {
            val graphics = image.createGraphics()
            graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
            )
            graphics.setRenderingHint(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON
            )

            // Page numbers
            graphics.color = Color.WHITE
            graphics.font = graphicsFonts.latoBlack.deriveFont(remapPixels(40).toFloat())
            ImageUtils.drawCenteredStringOutlined(
                graphics,
                albumPage.pageLeft.toString(),
                Rectangle(
                    remapPixels(0),
                    remapPixels(1146),
                    remapPixels(80),
                    remapPixels(54),
                ),
                graphics.font
            )

            ImageUtils.drawCenteredStringOutlined(
                graphics,
                albumPage.pageRight.toString(),
                Rectangle(
                    remapPixels(1600 - 80),
                    remapPixels(1146),
                    remapPixels(80),
                    remapPixels(54),
                ),
                graphics.font
            )

            for (stickerSlot in albumPage.slots) {
                val stickerInfo = alreadyStickedCards.firstOrNull { resultRow ->
                    resultRow[LoriCoolCardsEventCards.fancyCardId] == stickerSlot.stickerId
                }

                if (stickerInfo != null) {
                    // TODO: Preload the sticker images?
                    val alreadyStickedCard = ImageIO.read(URL(stickerInfo[LoriCoolCardsEventCards.cardFrontImageUrl]))
                    graphics.drawImage(
                        alreadyStickedCard,
                        remapPixels(stickerSlot.x),
                        remapPixels(stickerSlot.y),
                        remapPixels(stickerSlot.width),
                        remapPixels(stickerSlot.height),
                        null
                    )
                } else {
                    // Not sticked yet
                    graphics.color = Color(0, 0, 0, 90)
                    graphics.fillRect(
                        remapPixels(stickerSlot.x),
                        remapPixels(stickerSlot.y),
                        remapPixels(stickerSlot.width),
                        remapPixels(stickerSlot.height),
                    )

                    graphics.color = Color(0, 0, 0, 60)
                    repeat(stickerSlot.outline) {
                        graphics.drawRect(
                            remapPixels((stickerSlot.x) + it),
                            remapPixels((stickerSlot.y) + it),
                            remapPixels((stickerSlot.width) - (it * 2)),
                            remapPixels((stickerSlot.height) - (it * 2))
                        )
                    }

                    // Change the text color based on the background color
                    // This is not perfect (technically we need to check the entire background, not just one pixel), but...
                    // https://stackoverflow.com/a/3943023/7271796
                    val backgroundColor = Color(image.getRGB(remapPixels(stickerSlot.x), remapPixels(stickerSlot.y)))

                    graphics.color = if (backgroundColor.red * 0.299 + backgroundColor.green * 0.587 + backgroundColor.blue * 0.114 > 186) {
                        Color.BLACK
                    } else {
                        Color.WHITE
                    }

                    graphics.font = graphicsFonts.latoBlack.deriveFont(remapPixels(stickerSlot.stickerIdFontSize).toFloat())
                    ImageUtils.drawCenteredString(
                        graphics,
                        stickerSlot.stickerId,
                        Rectangle(
                            remapPixels(stickerSlot.x),
                            remapPixels(stickerSlot.y),
                            remapPixels(stickerSlot.width),
                            remapPixels(stickerSlot.height),
                        ),
                        graphics.font
                    )
                }
            }
        }

        return image
    }

    fun generateAlbumPreview(
        albumTemplate: StickerAlbumTemplate,
        alreadyStickedCards: List<ResultRow>,
        albumPage: StickerAlbumTemplate.AlbumComboPage
    ): ByteArray {
        val image = generateAlbumBase(albumTemplate, alreadyStickedCards, albumPage, 1.0f)

        return image.let {
            val baos = ByteArrayOutputStream()
            ImageIO.write(it, "png", baos)
            baos.toByteArray()
        }
    }

    fun generateStickerBeingStickedInAlbumGIF(
        albumTemplate: StickerAlbumTemplate,
        stickingCard: ResultRow,
        alreadyStickedCards: List<ResultRow>
    ): ByteArray {
        val albumPage = albumTemplate.getAlbumComboPageThatHasSticker(stickingCard[LoriCoolCardsEventCards.fancyCardId]) ?: error("Album does not contain sticker with ID ${stickingCard[LoriCoolCardsEventCards.fancyCardId]}")

        return generateStickerBeingStickedInAlbumGIF(
            albumTemplate,
            stickingCard,
            generateAlbumBase(albumTemplate, alreadyStickedCards, albumPage, 0.5f)
        )
    }

    fun generateStickerBeingStickedInAlbumGIF(
        albumTemplate: StickerAlbumTemplate,
        stickingCard: ResultRow,
        albumBaseImage: BufferedImage
    ): ByteArray {
        // TODO: Preload the sticker images?
        val generatedCard = ImageIO.read(URL(stickingCard[LoriCoolCardsEventCards.cardFrontImageUrl]))

        val start = Clock.System.now()

        val albumPage = albumTemplate.getAlbumComboPageThatHasSticker(stickingCard[LoriCoolCardsEventCards.fancyCardId]) ?: error("Album does not contain sticker with ID ${stickingCard[LoriCoolCardsEventCards.fancyCardId]}")

        val frames = mutableListOf<AlbumPasteFrame>()

        frames.add(
            AlbumPasteFrame(
                albumBaseImage,
                0,
                0
            )
        )

        // Create sticker fade animation
        repeat(20) {
            val whereShouldItBePastedSlot = albumPage.slots.first {
                it.stickerId == stickingCard[LoriCoolCardsEventCards.fancyCardId]
            }

            // We need to copy because "getSubimage" still uses the original image array
            val image = ImageUtils.copyToBufferedImageBGR(frames.first().image.getSubimage(whereShouldItBePastedSlot.x / 2, whereShouldItBePastedSlot.y / 2, whereShouldItBePastedSlot.width / 2, whereShouldItBePastedSlot.height / 2))

            val graphics = image.createGraphics()
            graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
            )
            graphics.drawImage(generatedCard, 0, 0, image.width, image.height, null)

            graphics.color = Color.WHITE
            graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((20 - it) / 20f))
            graphics.fillRect(0, 0, image.width, image.height)

            frames.add(
                AlbumPasteFrame(
                    image,
                    whereShouldItBePastedSlot.x / 2,
                    whereShouldItBePastedSlot.y / 2
                )
            )
        }

        val indexedFrames = mutableListOf<AlbumPasteFrame>()

        for ((index, frame) in frames.withIndex()) {
            // println("Rendering frame $index")

            val indexedImage = ImageUtils.convertToIndexedImage(frame.image, createStickerReceivedIndexColorModel())

            indexedFrames.add(
                AlbumPasteFrame(
                    indexedImage,
                    frame.xPosition,
                    frame.yPosition
                )
            )
        }

        val baos = ByteArrayOutputStream()

        val animatedGifEncoder = AnimatedGifEncoder(baos)

        animatedGifEncoder.start()
        animatedGifEncoder.delay = 5
        animatedGifEncoder.repeat = -1

        for ((index, indexedFrame) in indexedFrames.withIndex()) {
            val indexedImage = indexedFrame.image
            val rawIndexedPixelData = (indexedImage.raster.dataBuffer as DataBufferByte).data // get it straight from the source

            animatedGifEncoder.addFrameRaw(
                indexedImage.width,
                indexedImage.height,
                rawIndexedPixelData,
                if (index == 0) // The first frame ALWAYS needs the palette
                    AnimatedGifEncoder.FramePalette(
                        stickerReceivedColorTab,
                        8,
                        7,
                        -1
                    )
                else null, // But if we pass null for the rest of the frames, we can avoid writing the palette again, because that means we are using the global palette (yay!)
                xPosition = indexedFrame.xPosition,
                yPosition = indexedFrame.yPosition
            )
        }

        animatedGifEncoder.finish()

        // println("Took ${Clock.System.now() - start} to generate the album paste")

        return baos.toByteArray()
    }

    // This is an alternate version that sticks multiple stickers in a row
    fun generateStickerBeingStickedInAlbumGIF(
        albumTemplate: StickerAlbumTemplate,
        alreadyStickedCards: List<ResultRow>,
        stickingCards: List<ResultRow>
    ): ByteArray {
        val start = Clock.System.now()

        val frames = mutableListOf<AlbumPasteFrame>()

        // Mutable, we will insert newly sticked cards here
        val alreadyStickedCardsMutable = alreadyStickedCards.toMutableList()
        var currentPage: StickerAlbumTemplate.AlbumComboPage? = null

        // TODO: Preload the sticker images?
        for ((index, stickingCard) in stickingCards.withIndex()) {
            val albumPage = albumTemplate.getAlbumComboPageThatHasSticker(stickingCard[LoriCoolCardsEventCards.fancyCardId])
                ?: error("Album does not contain sticker with ID ${stickingCard[LoriCoolCardsEventCards.fancyCardId]}")

            val whereShouldItBePastedSlot = albumPage.slots.first {
                it.stickerId == stickingCard[LoriCoolCardsEventCards.fancyCardId]
            }

            // Optimization: If we are pasting on the same page as the previous sticker, we don't need to regenerate the album base!
            if (currentPage != albumPage) {
                currentPage = albumPage
                // TODO: Cache downloaded stickers for the current page
                val albumBaseImage = generateAlbumBase(
                    albumTemplate,
                    alreadyStickedCardsMutable,
                    albumPage,
                    0.5f
                )

                val albumBaseGraphics = albumBaseImage.createGraphics()
                albumBaseGraphics.color = Color.WHITE
                albumBaseGraphics.fillRect(
                    whereShouldItBePastedSlot.x / 2,
                    whereShouldItBePastedSlot.y / 2,
                    whereShouldItBePastedSlot.width / 2,
                    whereShouldItBePastedSlot.height / 2
                )
                // albumBaseGraphics.color = Color.BLACK
                // TODO: This breaks due to our optimization of reusing the album base
                //  Could be fixed (we would need to draw a rectangle and draw the text inside it) but eh we don't really need this
                // albumBaseGraphics.drawString("Figurinha ${index + 1} de ${stickingCards.size}", 20, 20)

                frames.add(
                    AlbumPasteFrame(
                        albumBaseImage,
                        0,
                        0
                    )
                )
            }

            val generatedCard = ImageIO.read(URL(stickingCard[LoriCoolCardsEventCards.cardFrontImageUrl]))

            // Create sticker fade animation
            repeat(20) {
                // We need to copy because "getSubimage" still uses the original image array
                val image = ImageUtils.copyToBufferedImageBGR(
                    frames.first().image.getSubimage(
                        whereShouldItBePastedSlot.x / 2,
                        whereShouldItBePastedSlot.y / 2,
                        whereShouldItBePastedSlot.width / 2,
                        whereShouldItBePastedSlot.height / 2
                    )
                )

                val graphics = image.createGraphics()
                graphics.setRenderingHint(
                    RenderingHints.KEY_INTERPOLATION,
                    RenderingHints.VALUE_INTERPOLATION_BILINEAR
                )
                graphics.drawImage(generatedCard, 0, 0, image.width, image.height, null)

                graphics.color = Color.WHITE
                graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ((20 - it) / 20f))
                graphics.fillRect(0, 0, image.width, image.height)

                frames.add(
                    AlbumPasteFrame(
                        image,
                        whereShouldItBePastedSlot.x / 2,
                        whereShouldItBePastedSlot.y / 2,
                        delay = if (it == 19) 40 else 5 // slow down on the last paste frame for the current sticker
                    )
                )
            }

            alreadyStickedCardsMutable.add(stickingCard)
        }

        val indexedFrames = mutableListOf<AlbumPasteFrame>()

        for ((index, frame) in frames.withIndex()) {
            // println("Rendering frame $index")

            val indexedImage = ImageUtils.convertToIndexedImage(frame.image, createStickerReceivedIndexColorModel())

            indexedFrames.add(
                AlbumPasteFrame(
                    indexedImage,
                    frame.xPosition,
                    frame.yPosition,
                    frame.delay
                )
            )
        }

        val baos = ByteArrayOutputStream()

        val animatedGifEncoder = AnimatedGifEncoder(baos)

        animatedGifEncoder.start()
        animatedGifEncoder.delay = 5
        animatedGifEncoder.repeat = -1

        for ((index, indexedFrame) in indexedFrames.withIndex()) {
            val indexedImage = indexedFrame.image
            val rawIndexedPixelData = (indexedImage.raster.dataBuffer as DataBufferByte).data // get it straight from the source

            animatedGifEncoder.addFrameRaw(
                indexedImage.width,
                indexedImage.height,
                rawIndexedPixelData,
                if (index == 0) // The first frame ALWAYS needs the palette
                    AnimatedGifEncoder.FramePalette(
                        stickerReceivedColorTab,
                        8,
                        7,
                        -1
                    )
                else null, // But if we pass null for the rest of the frames, we can avoid writing the palette again, because that means we are using the global palette (yay!)
                xPosition = indexedFrame.xPosition,
                yPosition = indexedFrame.yPosition,
                frameDelay = indexedFrame.delay ?: animatedGifEncoder.delay
            )
        }

        animatedGifEncoder.finish()

        // println("Took ${Clock.System.now() - start} to generate the album paste (multiple)")

        return baos.toByteArray()
    }

    fun generateStickerToBeStickedHighlightGIF(albumTemplate: StickerAlbumTemplate, stickingCard: ResultRow, alreadyStickedCards: List<ResultRow>): ByteArray {
        val albumPage = albumTemplate.getAlbumComboPageThatHasSticker(stickingCard[LoriCoolCardsEventCards.fancyCardId]) ?: error("Album does not contain sticker with ID ${stickingCard[LoriCoolCardsEventCards.fancyCardId]}")

        return generateStickerToBeStickedHighlightGIF(
            albumTemplate,
            stickingCard,
            generateAlbumBase(albumTemplate, alreadyStickedCards, albumPage, 0.5f)
        )
    }

    fun generateStickerToBeStickedHighlightGIF(
        albumTemplate: StickerAlbumTemplate,
        stickingCard: ResultRow,
        albumBaseImage: BufferedImage
    ): ByteArray {
        val albumPage = albumTemplate.getAlbumComboPageThatHasSticker(stickingCard[LoriCoolCardsEventCards.fancyCardId]) ?: error("Album does not contain sticker with ID ${stickingCard[LoriCoolCardsEventCards.fancyCardId]}")

        val frames = mutableListOf<AlbumPasteFrame>()

        frames.add(
            AlbumPasteFrame(
                albumBaseImage,
                0,
                0
            )
        )

        // Create sticker fade animation
        repeat(20) {
            val whereShouldItBePastedSlot = albumPage.slots.first {
                it.stickerId == stickingCard[LoriCoolCardsEventCards.fancyCardId]
            }

            // We need to copy because "getSubimage" still uses the original image array
            val image = ImageUtils.copyToBufferedImageBGR(
                frames.first().image.getSubimage(
                    whereShouldItBePastedSlot.x / 2,
                    whereShouldItBePastedSlot.y / 2,
                    whereShouldItBePastedSlot.width / 2,
                    whereShouldItBePastedSlot.height / 2
                )
            )

            val graphics = image.createGraphics()
            graphics.setRenderingHint(
                RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR
            )

            graphics.color = Color.WHITE
            graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, Easings.easeOutSine(it / 20.0).toFloat())
            graphics.fillRect(0, 0, image.width, image.height)

            frames.add(
                AlbumPasteFrame(
                    image,
                    whereShouldItBePastedSlot.x / 2,
                    whereShouldItBePastedSlot.y / 2
                )
            )
        }

        val indexedFrames = mutableListOf<AlbumPasteFrame>()

        for ((index, frame) in frames.withIndex()) {
            // println("Rendering frame $index")

            val indexedImage = ImageUtils.convertToIndexedImage(frame.image, createStickerReceivedIndexColorModel())

            indexedFrames.add(
                AlbumPasteFrame(
                    indexedImage,
                    frame.xPosition,
                    frame.yPosition
                )
            )
        }

        indexedFrames.addAll(
            indexedFrames.reversed()
                .drop(1)
                .dropLast(1)
        )
        val baos = ByteArrayOutputStream()

        val animatedGifEncoder = AnimatedGifEncoder(baos)

        animatedGifEncoder.start()
        animatedGifEncoder.delay = 4
        animatedGifEncoder.repeat = 0

        for ((index, indexedFrame) in indexedFrames.withIndex()) {
            val indexedImage = indexedFrame.image
            val rawIndexedPixelData = (indexedImage.raster.dataBuffer as DataBufferByte).data // get it straight from the source

            animatedGifEncoder.addFrameRaw(
                indexedImage.width,
                indexedImage.height,
                rawIndexedPixelData,
                if (index == 0) // The first frame ALWAYS needs the palette
                    AnimatedGifEncoder.FramePalette(
                        stickerReceivedColorTab,
                        8,
                        7,
                        -1
                    )
                else null, // But if we pass null for the rest of the frames, we can avoid writing the palette again, because that means we are using the global palette (yay!)
                xPosition = indexedFrame.xPosition,
                yPosition = indexedFrame.yPosition
            )
        }

        animatedGifEncoder.finish()

        return baos.toByteArray()
    }

    fun generateFrontFacingSticker(cardGenData: CardGenData): BufferedImage {
        val id = cardGenData.id
        val avatar = cardGenData.avatar
        val backgroundImage = cardGenData.backgroundImage
        val name = cardGenData.name
        val badgeLabel = cardGenData.badgeLabel
        val badgeIcon = cardGenData.badgeIconImage

        val rarity = cardGenData.cardRarity
        val colorJava = rarity.color.toJavaColor()
        val start = Clock.System.now()

        // TODO: proper bg crop
        //   (this actually should be cropped before passing the background to this function, right?)
        val background = backgroundImage.getResizedInstance(800, 600, InterpolationType.BILINEAR)
        val base = BufferedImage(stickerBase.width, stickerBase.height, BufferedImage.TYPE_INT_ARGB)

        val baseGraphics = base.createGraphics()
        baseGraphics.enableFontAntiAliasing()
        baseGraphics.drawImage(background.getResizedInstance(1614, 1209, InterpolationType.BILINEAR), -357, 36, null)

        // While the blur effect is cool, when it is used in the GIF... it gets really mushy
        /*
        // We need to extend the border to blur the edges, because if not Java will NOT blur them (with EDGE_NO_OP) or will fill with zeros
        // We will crop this later
        val blurredImage = ImageUtils.applyGaussianBlur(ImageUtils.extendBorder(background, 8), FRONT_FACING_CARD_BACKGROUND_BLUR_KERNEL)

        val g2d = blurredImage.createGraphics()

        // Set the composite to AlphaComposite.DstOut
        g2d.composite = AlphaComposite.getInstance(AlphaComposite.DST_OUT)

        // Create a gradient paint
        val gradient = GradientPaint(
            0f, 0f, Color(0, 0, 0, 255),
            0f, blurredImage.height.toFloat(), Color(0, 0, 0, 0)
        ) // End color (opaque)

        // Fill a rectangle with the gradient
        g2d.paint = gradient
        g2d.fillRect(0, 0, blurredImage.width, blurredImage.height)

        // ImageIO.write(blurredImage, "png", File("D:\\Pictures\\Loritta\\LoriCoolCards\\faded_bg.png"))

        baseGraphics.drawImage(
            blurredImage.getSubimage(8, 8, 800, 600).getResizedInstance(1614, 1209, InterpolationType.BILINEAR),
            -357,
            36,
            null
        ) */
        baseGraphics.drawImage(stickerBase, 0, 0, null)

        baseGraphics.drawImage(avatar.getResizedInstance(664, 664, InterpolationType.BILINEAR), 118, 209, null)

        run {
            lateinit var nameFont: Font

            val nameToBeRendered = name.uppercase()
            var length = Int.MAX_VALUE // Just make it big enough to trip the check below!
            var nextFontSize = 75

            while (length > 840) {
                // println("[name] current length is $length using $nextFontSize")

                // brute force a good font size
                nameFont = graphicsFonts.latoBlack.deriveFont(nextFontSize.toFloat())
                baseGraphics.font = nameFont
                val fontMetrics = baseGraphics.fontMetrics
                length = fontMetrics.stringWidth(nameToBeRendered)

                nextFontSize -= 1
            }

            // println("name length: $length")

            baseGraphics.color = Color.BLACK
            ImageUtils.drawCenteredString(
                baseGraphics,
                nameToBeRendered,
                Rectangle(30, 952, 840, 111),
                nameFont
            )
        }

        // ===[ EQUIPPED BADGE ]===
        run {
            if (badgeLabel != null) {
                lateinit var nameFont: Font

                val badgeNameToBeRendered = badgeLabel
                var length = Int.MAX_VALUE // Just make it big enough to trip the check below!
                var nextFontSize = 180
                val padding = 16
                var sidePadding = 128
                var badgeSize = Int.MAX_VALUE

                // Once again we need to brute force
                // Three -paddings because it is the left side + middle (between image and text) + right
                while (length > (840 - sidePadding - padding - sidePadding - badgeSize)) {
                    // println("[badge] current length is $length using $nextFontSize - side padding: $sidePadding")

                    // brute force a good font size
                    nameFont = graphicsFonts.latoBlack.deriveFont(nextFontSize.toFloat())
                    baseGraphics.font = nameFont
                    val fontMetrics = baseGraphics.fontMetrics
                    length = fontMetrics.stringWidth(badgeNameToBeRendered)

                    val frc = baseGraphics.fontRenderContext
                    // This seems weird but it is INTENTIONAL to get the proper ascent of the font
                    // We don't want to center in this like "Pagador de Aluguel" on the g descent
                    val gv: GlyphVector = baseGraphics.font.createGlyphVector(frc, "M")
                    val pixelBounds = gv.getPixelBounds(null, 0f, 0f)
                    badgeSize = pixelBounds.height

                    // Side padding changes depending on the current font size, min 8px tho
                    if (sidePadding != 8) {
                        sidePadding -= 1
                    }

                    nextFontSize -= 1
                }

                val fontMetrics = baseGraphics.fontMetrics
                baseGraphics.color = Color.WHITE

                val rect = Rectangle(30, 1063, 840, 182)

                // Determine the X coordinate for the text
                val textX = rect.x + (rect.width - fontMetrics.stringWidth(badgeNameToBeRendered)) / 2
                // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
                val textY = rect.y + (rect.height - fontMetrics.height) / 2 + fontMetrics.ascent
                // Draw the String

                baseGraphics.drawString(
                    badgeNameToBeRendered,
                    textX + ((padding + badgeSize) / 2),
                    textY
                )

                baseGraphics.drawImage(
                    badgeIcon,
                    textX - ((padding + badgeSize) / 2),
                    textY - badgeSize,
                    badgeSize,
                    badgeSize,
                    null
                )
            } else {
                baseGraphics.drawImage(
                    badgeFallbackOverlay,
                    0,
                    0,
                    null
                )
            }
        }

        // ===[ CARD ID ]===
        val cardId = graphicsFonts.latoBlack.deriveFont(35f)
        baseGraphics.color = colorJava
        baseGraphics.font = cardId
        baseGraphics.drawString("#$id", 30, 1274)

        // Set the blending rule to multiply
        val oldComposite = baseGraphics.composite
        val composite = MultiplyComposite()
        baseGraphics.composite = composite

        // Paste the image with multiply blending
        val overlay = when (rarity) {
            // Once upon a time each epic+ rarity had a different coloured overlay
            // This has been removed because the different colored overlays didn't look as cool as the legendary overlay
            CardRarity.EPIC, CardRarity.LEGENDARY, CardRarity.MYTHIC -> legendaryOverlay
            else -> null
        }

        if (overlay != null) {
            baseGraphics.drawImage(ImageUtils.copyToBufferedImageARGB(overlay), 0, 0, null)
        }

        baseGraphics.composite = oldComposite
        baseGraphics.drawImage(glareOverlay, 0, 0, null)

        return base
    }

    fun generateUnknownStickerGIF(): ByteArray {
        val background = BufferedImage(800, 600, BufferedImage.TYPE_INT_RGB)
        val backgroundGraphics = background.createGraphics()
        backgroundGraphics.color = Color.BLACK
        backgroundGraphics.fillRect(0, 0, 800, 600)

        val avatar = BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB)
        val avatarGraphics = avatar.createGraphics()
        avatarGraphics.color = Color.BLACK
        avatarGraphics.fillRect(0, 0, 256, 256)

        val frontFacingCard = generateFrontFacingSticker(
            CardGenData(
                "????",
                CardRarity.COMMON,
                "?",
                avatar,
                background,
                null,
                null
            )
        )

        val stickerReceivedGIF = generateStickerReceivedGIF(
            CardRarity.COMMON,
            frontFacingCard,
            StickerReceivedRenderType.UnknownStickerEvent
        )

        return stickerReceivedGIF
    }

    private fun generateStickerReceivedFrames(
        rarity: CardRarity,
        frontFacingCardImage: BufferedImage,
        imageRenderType: StickerReceivedRenderType,
        cropMiddleForGIFs: Boolean
    ): List<AlbumPasteFrame> {
        val colorJava = imageRenderType.backgroundColorOverride ?: rarity.color.toJavaColor()

        val sparkles1CompositeMode = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f)
        val sparkles2CompositeMode = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f)
        val sparkles3CompositeMode = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f)

        val baseTransform = LorittaImage(frontFacingCardImage)
        // width: 900px
        // height: 1280px;
        baseTransform.setCorners(0.0f, 60.0f, 750.0f, 0.0f, 900.0f, 1100.0f, 100.0f, 1280.0f)

        /* ImageIO.write(
            baseTransform.bufferedImage,
            "png",
            File("D:\\Pictures\\Loritta\\LoriCoolCards\\transformed_card.png")
        ) */

        val cloned = ImageUtils.copyToBufferedImageARGB(baseTransform.bufferedImage)
        for (x in 0 until cloned.width) {
            for (y in 0 until cloned.height) {
                val rgba = cloned.getRGB(x, y)
                val unpack = ImageUtils.unpackRGBA(rgba)

                cloned.setRGB(x, y, ImageUtils.packRGBA(0, 0, 0, unpack[3]))
            }
        }

        val f = mutableListOf<AlbumPasteFrame>()

        val frames = 20
        repeat(frames) {
            // This is stupid, we can't use the IndexColorModel directly because that causes dithering (thanks java, there is no workaround for this yet)
            // println("Frame $it")
            val newImage = BufferedImage(imageRenderType.width, imageRenderType.height, BufferedImage.TYPE_3BYTE_BGR)
            val bg = newImage.createGraphics()
            val originalComposite = bg.composite

            val cardY = (38 + Easings.easeInOutSine(it / frames.toDouble()) * 20).toInt()
            // Calculate the center of the image
            // (actually this is the "center of where the card should be rendered")
            val imageXCenter = if (imageRenderType is StickerReceivedRenderType.ProfileDesignWithInfo)
                (imageRenderType.width / 2) + 190 // 197 is the max we can go
            else
                (imageRenderType.width / 2)
            val cardX = imageXCenter - (441 / 2)

            if (imageRenderType is StickerReceivedRenderType.ProfileDesignWithInfo && imageRenderType.renderBackgroundCallbackOverride != null) {
                imageRenderType.renderBackgroundCallbackOverride.invoke(bg, cardX, cardY, imageRenderType)
            } else if (imageRenderType is StickerReceivedRenderType.ProfileDesignPlain && imageRenderType.renderBackgroundCallbackOverride != null) {
                imageRenderType.renderBackgroundCallbackOverride.invoke(bg, cardX, cardY)
            } else {
                bg.color = colorJava
                bg.fillRect(0, 0, newImage.width, newImage.height)

                val alphaComposite = AlphaComposite.getInstance(
                    AlphaComposite.SRC_OVER,
                    0.1f
                )
                bg.composite = alphaComposite

                var x = 0
                while (newImage.width > x) {
                    var y = 0
                    while (newImage.height > y) {
                        bg.drawImage(starPattern, x, y, null)
                        y += starPattern.height
                    }
                    x += starPattern.width
                }
            }

            // Reset to the original composite
            bg.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)

            // Set the color to 100% black
            bg.color = Color.BLACK

            bg.drawImage(
                cloned,
                cardX - 8,
                8 + cardY,
                441,
                627,
                null
            )

            // Reset to the original composite
            bg.composite = originalComposite

            bg.drawImage(
                baseTransform.bufferedImage,
                cardX,
                cardY,
                441,
                627,
                null
            )

            if (imageRenderType.renderSparkles) {
                if (it in 3..5) {
                    bg.composite = sparkles1CompositeMode
                    bg.drawImage(
                        sparkles1,
                        imageXCenter - 250,
                        cardY,
                        null
                    )
                }
                if (it in 6..8) {
                    bg.composite = sparkles2CompositeMode
                    bg.drawImage(
                        sparkles2,
                        imageXCenter - 250,
                        cardY,
                        null
                    )
                }
                if (it in 9..11) {
                    bg.composite = sparkles3CompositeMode
                    bg.drawImage(
                        sparkles3,
                        imageXCenter - 250,
                        cardY,
                        null
                    )
                }
                if (it in 12..14) {
                    bg.composite = sparkles2CompositeMode
                    bg.drawImage(
                        sparkles2,
                        imageXCenter - 250,
                        cardY,
                        null
                    )
                }
                if (it in 15..17) {
                    bg.composite = sparkles1CompositeMode
                    bg.drawImage(
                        sparkles1,
                        imageXCenter - 250,
                        cardY,
                        null
                    )
                }

                // bg.drawString("frame $it", 100, 100)
                if (it in 12..14) {
                    bg.composite = sparkles1CompositeMode
                    bg.drawImage(
                        sparkles1,
                        imageXCenter + 150,
                        cardY + 400,
                        null
                    )
                }
                if (it in 15..17) {
                    bg.composite = sparkles2CompositeMode
                    bg.drawImage(
                        sparkles2,
                        imageXCenter + 150,
                        cardY + 400,
                        null
                    )
                }
                if (it in 18..19) {
                    bg.composite = sparkles3CompositeMode
                    bg.drawImage(
                        sparkles3,
                        imageXCenter + 150,
                        cardY + 400,
                        null
                    )
                }
            }

            if (imageRenderType is StickerReceivedRenderType.ProfileDesignWithInfo) {
                imageRenderType.additionalRenderPostCallback.invoke(bg, cardX, cardY, imageRenderType)
            }

            // Calculated by manually checking the image in Photoshop
            val modificationsStartAtX = imageXCenter - 240
            val modificationsEndAtX = imageXCenter + 283
            val modificationsWidth = modificationsEndAtX - modificationsStartAtX

            // It would be better if we just animated the changed parts instead of everything, but oh well
            // We will crop the edited image into a subimage
            f.add(
                AlbumPasteFrame(
                    // We copy the cropped image because we do not want to share an array to avoid issues when we use the backing array directly
                    if (it == 0 || !cropMiddleForGIFs) newImage else ImageUtils.copyToBufferedImageBGR(newImage.getSubimage(modificationsStartAtX, 0, modificationsWidth, 720)),
                    if (it == 0) 0 else modificationsStartAtX,
                    0
                )
            )

            // ImageIO.write(newImage, "png", File("D:\\Pictures\\Loritta\\LoriCoolCards\\frames\\${it}.png"))
            // bg.color = Color(Random().nextInt(0, 255), Random().nextInt(0, 255), Random().nextInt(0, 255))
            // bg.fillRect(0, 0, newImage.width, newImage.height)

            // ImageIO.write(newImage, "png", File("D:\\Pictures\\Loritta\\LoriCoolCards\\bg.png"))
        }

        return f
    }

    fun generateStickerReceivedGIF(
        rarity: CardRarity,
        frontFacingCardImage: BufferedImage,
        imageRenderType: StickerReceivedRenderType
    ): ByteArray {
        val f = generateStickerReceivedFrames(
            rarity,
            frontFacingCardImage,
            imageRenderType,
            true
        )

        val baos = ByteArrayOutputStream()

        // TODO: We need to figure out a way to make a FIXED palette GIF
        // TODO: Maybe to do that, because we know that every frame should ALWAYS have the same color, use a HashMap lookup color table?
        // TODO: Because this is a ping-pong animation, we can cache the written pixels and just rewrite it
        val animatedGifEncoder = AnimatedGifEncoder(baos)

        animatedGifEncoder.start()
        animatedGifEncoder.delay = 5
        animatedGifEncoder.repeat = 0

        val indexedFrames = mutableListOf<AlbumPasteFrame>()

        for ((index, frame) in f.withIndex()) {
            // println("Rendering frame $index")

            val indexedImage = ImageUtils.convertToIndexedImage(frame.image, createStickerReceivedIndexColorModel())

            indexedFrames.add(
                AlbumPasteFrame(
                    indexedImage,
                    frame.xPosition,
                    frame.yPosition
                )
            )
        }

        // add backwards
        // we need to remove the first and last frames
        indexedFrames.addAll(
            indexedFrames
                .drop(1)
                .dropLast(1)
                .reversed()
        )

        for ((index, indexedFrame) in indexedFrames.withIndex()) {
            val indexedImage = indexedFrame.image
            val rawIndexedPixelData = (indexedImage.raster.dataBuffer as DataBufferByte).data // get it straight from the source

            animatedGifEncoder.addFrameRaw(
                indexedImage.width,
                indexedImage.height,
                rawIndexedPixelData,
                if (index == 0) // The first frame ALWAYS needs the palette
                    AnimatedGifEncoder.FramePalette(
                        stickerReceivedColorTab,
                        8,
                        7,
                        -1
                    )
                else null, // But if we pass null for the rest of the frames, we can avoid writing the palette again, because that means we are using the global palette (yay!)
                xPosition = indexedFrame.xPosition,
                yPosition = indexedFrame.yPosition
            )
        }

        animatedGifEncoder.finish()

        return baos.toByteArray()
    }

    fun generateStickerReceivedWEBP(
        loritta: LorittaBot,
        rarity: CardRarity,
        frontFacingCardImage: BufferedImage,
        imageRenderType: StickerReceivedRenderType
    ): ByteArray {
        val f = generateStickerReceivedFrames(
            rarity,
            frontFacingCardImage,
            imageRenderType,
            false
        )

        val id = UUID.randomUUID()
        val fileOutput = File("${loritta.config.loritta.folders.temp}\\profile-$id.webp")

        val processBuilder = ProcessBuilder(
            loritta.config.loritta.binaries.ffmpeg,
            "-framerate",
            "20",
            "-f",
            "rawvideo",
            "-pixel_format",
            "bgr24", // This is what the "BufferedImage.TYPE_3BYTE_BGR" uses behind the scenes
            "-video_size",
            "960x720",
            "-i",
            "-", // We will write to output stream
            "-c:v",
            "libwebp",
            "-preset",
            "none",
            "-loop",
            "0", // always loop
            "-quality",
            "85", // this is the default quality in img2webp
            "-compression_level",
            "4", // less = bigger file size, faster
            "-y",
            // Due to the way WEBP containers work (it goes back after writing all data! like mp4 containers), we need to write directly to a file
            fileOutput.toString()
        ).redirectErrorStream(true)
            .start()

        thread {
            while (true) {
                val r = processBuilder.inputStream.read()
                if (r == -1) // Keep reading until end of input
                    return@thread

                // TODO: I think we should remove this later...
                print(r.toChar())
            }
        }

        val indexedFrames = mutableListOf<AlbumPasteFrame>()

        indexedFrames.addAll(f)

        // add backwards
        // we need to remove the first and last frames
        indexedFrames.addAll(
            indexedFrames
                .drop(1)
                .dropLast(1)
                .reversed()
        )

        for (frame in indexedFrames) {
            // println("Writing frame $frame")
            processBuilder.outputStream.write((frame.image.raster.dataBuffer as DataBufferByte).data)
            processBuilder.outputStream.flush()
        }

        processBuilder.outputStream.close()
        processBuilder.waitFor()

        val bytes = fileOutput.readBytes()
        fileOutput.delete()

        return bytes
    }

    fun generateBuyingBoosterPackGIF(): ByteArray {
        val s = Clock.System.now()

        // yeah, we need to reduce the colors somehow
        val cardPalette = ImageUtils.convertToPaletteList(ImageIO.read(File("D:\\Pictures\\Loritta\\LoriCoolCards\\buying_package_palette.png")))

        val sparkles1CompositeMode = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f)
        val sparkles2CompositeMode = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.4f)
        val sparkles3CompositeMode = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.6f)

        val colorTab = cardPalette.map {
            listOf(
                it.red.toByte(),
                it.green.toByte(),
                it.blue.toByte()
            )
        }.flatten().toByteArray()

        val frames = mutableListOf<AlbumPasteFrame>()

        val stickerPackage = ImageIO.read(File("D:\\Pictures\\Loritta\\LoriCoolCards\\mrbeast-feastables-tcg-pack-mockup-v3.png"))

        // Create shadowy version
        val shadowVersion = ImageUtils.copyToBufferedImageARGB(stickerPackage)
        for (x in 0 until shadowVersion.width) {
            for (y in 0 until shadowVersion.height) {
                val rgba = shadowVersion.getRGB(x, y)
                val unpack = ImageUtils.unpackRGBA(rgba)

                shadowVersion.setRGB(x, y, ImageUtils.packRGBA(0, 0, 0, unpack[3]))
            }
        }

        repeat(80) {
            val newImage = BufferedImage(1280, 720, BufferedImage.TYPE_3BYTE_BGR)
            val graphics = newImage.createGraphics()

            val stickerTargetWidth = (stickerPackage.width / 3.25).toInt()
            val stickerTargetHeight = (stickerPackage.height / 3.25).toInt()

            // We hardcode after frame 40 to avoid jitters in the scale animation (the jitters happens because we can't draw on "subpixels")
            val easingRotate = if (it >= 40) 1.0 else Easings.easeOutElastic((it / 80.0), 2.0, 3.0, 3.0)

            val originalComposite = graphics.composite
            val originalTransform = graphics.transform

            graphics.color = CardRarity.RARE.color.toJavaColor()
            graphics.fillRect(0, 0, newImage.width, newImage.height)

            graphics.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.2f)

            var x = 0
            while (newImage.width > x) {
                var y = 0
                while (newImage.height > y) {
                    graphics.drawImage(scissorsPattern, x, y, null)
                    y += scissorsPattern.height
                }
                x += scissorsPattern.width
            }

            val alphaComposite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.5f)
            graphics.composite = alphaComposite

            val scaledTargetWidth = (stickerTargetWidth * easingRotate).toInt()
            val scaledTargetHeight = (stickerTargetHeight * easingRotate).toInt()

            val stickerPackageX = (newImage.width / 2) - (scaledTargetWidth / 2)
            val stickerPackageY = ((newImage.height / 2) - scaledTargetHeight / 2).toInt()

            graphics.rotate(
                Math.toRadians(330 + (easingRotate * 40)),
                (stickerPackageX + (scaledTargetWidth / 2)).toDouble(),
                (stickerPackageY + (scaledTargetHeight / 2)).toDouble()
            )

            graphics.drawImage(
                shadowVersion,
                stickerPackageX + (8 * easingRotate).toInt(),
                stickerPackageY + (8 * easingRotate).toInt(),
                scaledTargetWidth,
                scaledTargetHeight,
                null
            )

            graphics.composite = originalComposite
            graphics.drawImage(
                stickerPackage,
                stickerPackageX,
                stickerPackageY,
                scaledTargetWidth,
                scaledTargetHeight,
                null
            )

            graphics.transform = originalTransform // Reset rotation

            if (it in 40 until 44) {
                graphics.composite = sparkles1CompositeMode
                graphics.drawImage(
                    sparkles1,
                    422,
                    0,
                    null
                )
            }
            if (it in 44 until 48) {
                graphics.composite = sparkles2CompositeMode
                graphics.drawImage(
                    sparkles2,
                    422,
                    0,
                    null
                )
            }
            if (it in 48 until 52) {
                graphics.composite = sparkles3CompositeMode
                graphics.drawImage(
                    sparkles3,
                    422,
                    0,
                    null
                )
            }
            if (it in 52 until 56) {
                graphics.composite = sparkles2CompositeMode
                graphics.drawImage(
                    sparkles2,
                    422,
                    0,
                    null
                )
            }
            if (it in 56 until 60) {
                graphics.composite = sparkles1CompositeMode
                graphics.drawImage(
                    sparkles1,
                    422,
                    0,
                    null
                )
            }

            if (it in 56 until 60) {
                graphics.composite = sparkles1CompositeMode
                graphics.drawImage(
                    sparkles1,
                    721,
                    520,
                    null
                )
            }
            if (it in 60 until 64) {
                graphics.composite = sparkles2CompositeMode
                graphics.drawImage(
                    sparkles2,
                    721,
                    520,
                    null
                )
            }
            if (it in 64 until 68) {
                graphics.composite = sparkles3CompositeMode
                graphics.drawImage(
                    sparkles3,
                    721,
                    520,
                    null
                )
            }
            if (it in 68 until 72) {
                graphics.composite = sparkles2CompositeMode
                graphics.drawImage(
                    sparkles2,
                    721,
                    520,
                    null
                )
            }
            if (it in 72 until 76) {
                graphics.composite = sparkles1CompositeMode
                graphics.drawImage(
                    sparkles1,
                    721,
                    520,
                    null
                )
            }

            // ImageIO.write(newImage, "png", File("D:\\Pictures\\Loritta\\LoriCoolCards\\buying_package\\frame_$it.png"))
            frames.add(
                AlbumPasteFrame(
                    newImage,
                    0,
                    0
                )
            )
        }

        // We reduce the palette because GIF uses LSW, so similar patterns = good
        // By reducing it, we can have more "bigger" patches of same color, so the GIF is smaller!
        val cm = IndexColorModel(
            3,  // 3 bits can store up to 8 colors
            cardPalette.size,
            cardPalette.map { it.red.toByte() }.toByteArray(),
            cardPalette.map { it.green.toByte() }.toByteArray(),
            cardPalette.map { it.blue.toByte() }.toByteArray(),
            cardPalette.map { it.alpha.toByte() }.toByteArray()
        )

        val indexedFrames = mutableListOf<AlbumPasteFrame>()

        for ((index, frame) in frames.withIndex()) {
            // println("Rendering frame $index")

            val indexedImage = ImageUtils.convertToIndexedImage(frame.image, cm)

            indexedFrames.add(
                AlbumPasteFrame(
                    indexedImage,
                    frame.xPosition,
                    frame.yPosition,
                )
            )
        }

        val baos = ByteArrayOutputStream()

        val animatedGifEncoder = AnimatedGifEncoder(baos)

        animatedGifEncoder.start()
        animatedGifEncoder.delay = 3
        animatedGifEncoder.repeat = -1

        for ((index, indexedFrame) in indexedFrames.withIndex()) {
            val indexedImage = indexedFrame.image
            val rawIndexedPixelData = (indexedImage.raster.dataBuffer as DataBufferByte).data // get it straight from the source

            animatedGifEncoder.addFrameRaw(
                indexedImage.width,
                indexedImage.height,
                rawIndexedPixelData,
                if (index == 0) // The first frame ALWAYS needs the palette
                    AnimatedGifEncoder.FramePalette(
                        colorTab,
                        8,
                        7,
                        -1
                    )
                else null, // But if we pass null for the rest of the frames, we can avoid writing the palette again, because that means we are using the global palette (yay!)
                xPosition = indexedFrame.xPosition,
                yPosition = indexedFrame.yPosition
            )
        }

        animatedGifEncoder.finish()

        // println("Finished in ${Clock.System.now() - s}")

        return baos.toByteArray()
    }

    /**
     * The [IndexColorModel] used for the "Sticker received GIF" and other GIFs related to stickers
     *
     * We need to create a new [IndexColorModel] every time, because [IndexColorModel] uses synchronized in some of its methods, and that
     * causes a performance penalty when using the code in a threaded environment (not *really* a penalty, but we don't get any performance increase
     * with threading)
     */
// We reduce the palette because GIF uses LSW, so similar patterns = good
// By reducing it, we can have more "bigger" patches of same color, so the GIF is smaller!
    fun createStickerReceivedIndexColorModel() = IndexColorModel(
        3,  // 3 bits can store up to 8 colors
        srb2Palette.size,
        srb2Palette.map { it.red.toByte() }.toByteArray(),
        srb2Palette.map { it.green.toByte() }.toByteArray(),
        srb2Palette.map { it.blue.toByte() }.toByteArray(),
        srb2Palette.map { it.alpha.toByte() }.toByteArray()
    )

    data class AlbumPasteFrame(
        val image: BufferedImage,
        val xPosition: Int,
        val yPosition: Int,
        val delay: Int? = null
    )

    data class CardGenData(
        val id: String,
        val cardRarity: CardRarity,
        val name: String,
        val avatar: BufferedImage,
        val backgroundImage: BufferedImage,
        val badgeLabel: String?,
        val badgeIconImage: BufferedImage?,
    )

    sealed class StickerReceivedRenderType(
        val width: Int,
        val height: Int,
        val renderSparkles: Boolean,
        val backgroundColorOverride: Color?
    ) {
        /**
         * The sticker received GIF render type used for the sticker events (like in the "/figurittas buy" command)
         */
        data object LoriCoolCardsEvent : StickerReceivedRenderType(1280, 720, true, null)

        /**
         * The sticker received GIF render type used when looking up an unknown sticker (like in the "/figurittas view" command)
         */
        data object UnknownStickerEvent : StickerReceivedRenderType(1280, 720, false, Color(47, 47, 47))

        /**
         * The sticker received GIF render type design used for user profiles, this is the version without any bells and whistles
         */
        class ProfileDesignPlain(
            /**
             * The callback that will be invoked when rendering the background, if null, the default LoriCoolCards background will be used
             */
            val renderBackgroundCallbackOverride: ((graphics2d: Graphics2D, cardX: Int, cardY: Int) -> (Unit))? = null,
        ) : StickerReceivedRenderType(960, 720, true, null)

        /**
         * The sticker received GIF render type design used for user profiles, this is the version containing user stats
         */
        class ProfileDesignWithInfo(
            /**
             * The callback that will be invoked when rendering the background, if null, the default LoriCoolCards background will be used
             */
            val renderBackgroundCallbackOverride: ((graphics2d: Graphics2D, cardX: Int, cardY: Int, imageRenderType: ProfileDesignWithInfo) -> (Unit))? = null,

            /**
             * The callback that will be invoked after rendering the frame
             */
            val additionalRenderPostCallback: (graphics2d: Graphics2D, cardX: Int, cardY: Int, imageRenderType: ProfileDesignWithInfo) -> (Unit)
        ) : StickerReceivedRenderType(960, 720, true, null)
    }
}