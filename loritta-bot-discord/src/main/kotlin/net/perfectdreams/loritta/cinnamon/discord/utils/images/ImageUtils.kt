package net.perfectdreams.loritta.cinnamon.discord.utils.images

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.perfectdreams.gabrielaimageserver.exceptions.ContentLengthTooLargeException
import net.perfectdreams.gabrielaimageserver.exceptions.ImageTooLargeException
import net.perfectdreams.loritta.cinnamon.discord.utils.DiscordRegexes
import net.perfectdreams.loritta.cinnamon.discord.utils.UnicodeEmojiManager
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.SimpleImageInfo
import net.perfectdreams.loritta.morenitta.utils.readAllBytes
import java.awt.*
import java.awt.geom.RoundRectangle2D
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.net.HttpURLConnection
import java.net.URL
import javax.imageio.ImageIO
import kotlin.streams.toList

object ImageUtils {
    private val logger = KotlinLogging.logger {}
    private const val USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:61.0) Gecko/20100101 Firefox/61.0"
    val DEFAULT_DISCORD_AVATAR = runBlocking { readImageFromResources("/avatars/0.png") }

    /**
     * List of drawable types that are in unicode, and the user expects them to be drawn correctly (text, emojis, etc)
     */
    val ALLOWED_UNICODE_DRAWABLE_TYPES = listOf(DrawableType.TEXT, DrawableType.UNICODE_EMOJI)

    /**
     * Converts a given Image into a BufferedImage
     *
     * @param img The Image to be converted
     * @return The converted BufferedImage
     */
    fun toBufferedImage(img: Image): BufferedImage {
        if (img is BufferedImage) {
            return img
        }

        // Create a buffered image with transparency
        val bimage = BufferedImage(img.getWidth(null), img.getHeight(null), BufferedImage.TYPE_INT_ARGB)

        // Draw the image on to the buffered image
        val bGr = bimage.createGraphics()
        bGr.drawImage(img, 0, 0, null)
        bGr.dispose()

        // Return the buffered image
        return bimage
    }

    /**
     * Parses the [text] to multiple drawable sections
     */
    fun parseStringToDrawableSections(
        unicodeEmojiManager: UnicodeEmojiManager,
        text: String,
        allowedDrawableTypes: List<DrawableType> = DrawableType.values().toList()
    ): MutableList<DrawableSection> {
        val sections = mutableListOf<DrawableSection>()

        val matches = mutableListOf<RegexMatch>()

        if (DrawableType.DISCORD_EMOJI in allowedDrawableTypes) {
            DiscordRegexes.DiscordEmote.findAll(text)
                .forEach {
                    matches.add(DiscordEmoteRegexMatch(it))
                }
        }

        if (DrawableType.UNICODE_EMOJI in allowedDrawableTypes) {
            unicodeEmojiManager.regex.findAll(text)
                .forEach {
                    matches.add(UnicodeEmoteRegexMatch(it))
                }
        }

        var firstMatchedCharacterIndex = 0
        var lastMatchedCharacterIndex = 0

        for (match in matches.sortedBy { it.match.range.first }) {
            val matchResult = match.match
            if (DrawableType.TEXT in allowedDrawableTypes) {
                sections.add(
                    DrawableText(
                        text.substring(
                            firstMatchedCharacterIndex until matchResult.range.first
                        )
                    )
                )
            }

            when (match) {
                is DiscordEmoteRegexMatch -> {
                    val animated = matchResult.groupValues[1] == "a"
                    val emoteName = matchResult.groupValues[2]
                    val emoteId = matchResult.groupValues[3].toLong()
                    sections.add(DrawableDiscordEmote(emoteId, animated))
                }
                is UnicodeEmoteRegexMatch -> {
                    sections.add(DrawableUnicodeEmote(matchResult.value))
                }
            }

            lastMatchedCharacterIndex = matchResult.range.last + 1
            firstMatchedCharacterIndex = lastMatchedCharacterIndex
        }

        if (DrawableType.TEXT in allowedDrawableTypes) {
            sections.add(
                DrawableText(
                    text.substring(
                        lastMatchedCharacterIndex until text.length
                    )
                )
            )
        }

        return sections
    }

    suspend fun drawString(
        loritta: LorittaBot,
        graphics: Graphics,
        text: String,
        x: Int,
        y: Int,
        allowedDrawableTypes: List<DrawableType> = DrawableType.values().toList()
    ) = drawString(
        loritta.unicodeEmojiManager,
        loritta.emojiImageCache,
        graphics,
        text,
        x,
        y,
        allowedDrawableTypes
    )

    suspend fun drawString(
        unicodeEmojiManager: UnicodeEmojiManager,
        emojiImageCache: EmojiImageCache,
        graphics: Graphics,
        text: String,
        x: Int,
        y: Int,
        allowedDrawableTypes: List<DrawableType> = DrawableType.values().toList()
    ) {
        val sections = parseStringToDrawableSections(unicodeEmojiManager, text, allowedDrawableTypes)

        val fontMetrics = graphics.fontMetrics
        val emojiWidth = fontMetrics.ascent
        val emojiYOffset = (fontMetrics.descent / 2)

        var currentX = x // X atual
        val currentY = y // Y atual

        for (section in sections) {
            when (section) {
                is DrawableText -> {
                    val split = section.text.split("((?<= )|(?= ))".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() // Nós precisamos deixar os espaços entre os splits!
                    for (str in split) {
                        var width = fontMetrics.stringWidth(str) // Width do texto que nós queremos colocar
                        for (c in str.toCharArray()) { // E agora nós iremos printar todos os chars
                            width = fontMetrics.charWidth(c)
                            if (!graphics.font.canDisplay(c))
                                continue
                            graphics.drawString(c.toString(), currentX, currentY) // Escreva o char na imagem
                            currentX += width // E adicione o width no nosso currentX
                        }
                    }
                }
                is DrawableDiscordEmote -> {
                    val emoteImage = emojiImageCache.getDiscordEmoji(section.emoteId, 64)

                    if (emoteImage != null) {
                        graphics.drawImage(
                            emoteImage.getResizedInstance(emojiWidth, emojiWidth, InterpolationType.BILINEAR),
                            currentX,
                            currentY - emojiWidth + emojiYOffset,
                            null
                        )

                        currentX += emojiWidth
                    }
                }
                is DrawableUnicodeEmote -> {
                    val emoteImage = emojiImageCache.getTwitterEmoji(section.emoji.codePoints().toList())

                    if (emoteImage != null) {
                        graphics.drawImage(
                            emoteImage.getResizedInstance(emojiWidth, emojiWidth, InterpolationType.BILINEAR),
                            currentX,
                            currentY - emojiWidth + emojiYOffset,
                            null
                        )

                        currentX += emojiWidth
                    }
                }
            }
        }
    }

    fun drawStringAndShortenWithEllipsisIfOverflow(graphics: Graphics, text: String, x: Int, y: Int, maxX: Int? = null) {
        val fontMetrics = graphics.fontMetrics
        val font = graphics.font

        var currentX = x // X atual
        val currentY = y // Y atual
        var textToBeDrawn = text

        if (maxX != null) {
            var _text = ""
            var _x = x

            for (c in textToBeDrawn.toCharArray()) {
                val width = fontMetrics.charWidth(c) // Width do char (normalmente é 16)

                if (_x + width > maxX) {
                    _text = _text.substring(0, _text.length - 4)
                    _text += "..."
                    break
                }
                _text += c
                _x += width
            }

            textToBeDrawn = _text
        }

        for ((idx, c) in textToBeDrawn.toCharArray().withIndex()) {
            val width = fontMetrics.charWidth(c) // Width do char (normalmente é 16)
            if (!font.canDisplay(c)) {
                // Talvez seja um emoji!
                // TODO: Fix this
                /* val emoteImage = ImageUtils.getTwitterEmoji(textToBeDrawn, idx)
                if (emoteImage != null) {
                    graphics.drawImage(emoteImage.getScaledInstance(this.font.size, this.font.size, BufferedImage.SCALE_SMOOTH), currentX, currentY - this.font.size + 1, null)
                    currentX += fontMetrics.maxAdvance
                } */

                continue
            }
            graphics.drawString(c.toString(), currentX, currentY) // Escreva o char na imagem
            currentX += width // E adicione o width no nosso currentX
        }
    }

    /**
     * Escreve um texto em um Graphics, fazendo wrap caso necessário
     *
     * Esta versão separa entre espaços o texto, para ficar mais bonito
     *
     * @param text Texto
     * @param startX X inicial
     * @param startY Y inicial
     * @param endX X máximo, caso o texto ultrapasse o endX, ele automaticamente irá fazer wrap para a próxima linha
     * @param endY Y máximo, atualmente unused
     * @param fontMetrics Metrics da fonte
     * @param graphics Graphics usado para escrever a imagem
     * @return Y final
     */
    fun drawStringWrapSpaces(text: String, startX: Int, startY: Int, endX: Int, endY: Int, fontMetrics: FontMetrics, graphics: Graphics): Int {
        val lineHeight = fontMetrics.height // Aqui é a altura da nossa fonte

        var currentX = startX // X atual
        var currentY = startY // Y atual

        val split = text.split("((?<= )|(?= ))".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray() // Nós precisamos deixar os espaços entre os splits!
        for (str in split) {
            var width = fontMetrics.stringWidth(str) // Width do texto que nós queremos colocar
            if (currentX + width > endX) { // Se o currentX é maior que o endX... (Nós usamos currentX + width para verificar "ahead of time")
                currentX = startX // Nós iremos fazer wrapping do texto
                currentY += lineHeight
            }
            var idx = 0
            for (c in str.toCharArray()) { // E agora nós iremos printar todos os chars
                idx++
                if (c == '\n') {
                    currentX = startX // Nós iremos fazer wrapping do texto
                    currentY += lineHeight
                    continue
                }
                width = fontMetrics.charWidth(c)
                if (!graphics.font.canDisplay(c)) {
                    // Talvez seja um emoji!
                    // TODO: Fix this
                    // val emoteImage = getTwitterEmoji(str, idx)
                    // if (emoteImage != null) {
                    //     graphics.drawImage(emoteImage.getScaledInstance(width, width, BufferedImage.SCALE_SMOOTH), currentX, currentY - width, null)
                    //     currentX += width
                    // }

                    continue
                }
                graphics.drawString(c.toString(), currentX, currentY) // Escreva o char na imagem
                currentX += width // E adicione o width no nosso currentX
            }
        }
        return currentY
    }

    /**
     * Draw a String centered in the middle of a Rectangle.
     *
     * @param graphics The Graphics instance.
     * @param text The String to draw.
     * @param rect The Rectangle to center the text in.
     */
    suspend fun drawCenteredString(
        loritta: LorittaBot,
        graphics: Graphics,
        text: String,
        rect: Rectangle,
        font: Font = graphics.font,
        allowedDrawableTypes: List<DrawableType> = DrawableType.values().toList()
    ) {
        // Get the FontMetrics
        val metrics = graphics.getFontMetrics(font)
        // Determine the X coordinate for the text
        val x = rect.x + (rect.width - metrics.stringWidth(text)) / 2
        // Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
        val y = rect.y + (rect.height - metrics.height) / 2 + metrics.ascent
        // Draw the String

        drawString(loritta, graphics, text, x, y, allowedDrawableTypes)
    }

    /**
     * Draws a string with a outline around it, the text will be drawn with the current color set in the graphics object
     *
     * @param graphics     the image graphics
     * @param text         the text that will be drawn
     * @param x            where the text will be drawn in the x-axis
     * @param y            where the text will be drawn in the y-axis
     * @param outlineColor the color of the outline
     * @param power        the thickness of the outline
     */
    fun drawStringWithOutline(graphics: Graphics, text: String, x: Int, y: Int, outlineColor: Color = Color.BLACK, power: Int = 2) {
        val originalColor = graphics.color
        graphics.color = outlineColor
        for (powerX in -power..power) {
            for (powerY in -power..power) {
                graphics.drawString(text, x + powerX, y + powerY)
            }
        }

        graphics.color = originalColor
        graphics.drawString(text, x, y)
    }

    fun makeRoundedCorners(image: BufferedImage, cornerRadius: Int): BufferedImage {
        // https://stackoverflow.com/a/7603815/7271796
        val w = image.width
        val h = image.height
        val output = BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB)

        val g2 = output.createGraphics()

        // This is what we want, but it only does hard-clipping, i.e. aliasing
        // g2.setClip(new RoundRectangle2D ...)

        // so instead fake soft-clipping by first drawing the desired clip shape
        // in fully opaque white with antialiasing enabled...
        g2.composite = AlphaComposite.Src
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
        g2.color = Color.WHITE
        g2.fill(RoundRectangle2D.Float(0f, 0f, w.toFloat(), h.toFloat(), cornerRadius.toFloat(), cornerRadius.toFloat()))

        // ... then compositing the image on top,
        // using the white shape from above as alpha source
        g2.composite = AlphaComposite.SrcIn // https://stackoverflow.com/a/32989394/7271796
        g2.drawImage(image, 0, 0, null)

        g2.dispose()

        return output
    }

    /**
     * Downloads an image and returns it as a BufferedImage, additional checks are made and can be customized to avoid
     * downloading unsafe/big images that crash the application.
     *
     * @param url                            the image URL
     * @param connectTimeout                 the connection timeout
     * @param readTimeout                    the read timeout
     * @param maxSize                        the image's maximum size
     * @param overrideTimeoutsForSafeDomains if the URL is a safe domain, ignore timeouts
     * @param maxWidth                       the image's max width
     * @param maxHeight                      the image's max height
     * @param bypassSafety                   if the safety checks should be bypassed
     *
     * @return the image as a BufferedImage or null, if the image is considered unsafe
     */
    @JvmOverloads
    suspend fun downloadImage(url: String, connectTimeout: Int = 10, readTimeout: Int = 60, maxSize: Int = 8_388_608 /* 8mib */, overrideTimeoutsForSafeDomains: Boolean = false, maxWidth: Int = 2_500, maxHeight: Int = 2_500, bypassSafety: Boolean = false): BufferedImage? {
        val imageUrl = URL(url)
        val connection = imageUrl.openConnection() as HttpURLConnection

        connection.setRequestProperty(
            "User-Agent",
            USER_AGENT
        )

        val contentLength = connection.getHeaderFieldInt("Content-Length", 0)

        if (contentLength > maxSize) {
            logger.warn { "Image $url exceeds the maximum allowed Content-Length! ${connection.getHeaderFieldInt("Content-Length", 0)} > $maxSize"}
            throw ContentLengthTooLargeException()
        }

        if (connectTimeout != -1) {
            connection.connectTimeout = connectTimeout
        }

        if (readTimeout != -1) {
            connection.readTimeout = readTimeout
        }

        logger.debug { "Reading image $url; connectTimeout = $connectTimeout; readTimeout = $readTimeout; maxSize = $maxSize bytes; overrideTimeoutsForSafeDomains = $overrideTimeoutsForSafeDomains; maxWidth = $maxWidth; maxHeight = $maxHeight"}

        val imageBytes = try {
            withContext(Dispatchers.IO) {
                if (contentLength != 0) {
                    // If the Content-Length is known (example: images on Discord's CDN do have Content-Length on the response header)
                    // we can allocate the array with exactly the same size that the Content-Length provides, this way we avoid a lot of unnecessary Arrays.copyOf!
                    // Of course, this could be abused to allocate a gigantic array that causes Loritta to crash, but if the Content-Length is present, Loritta checks the size
                    // before trying to download it, so no worries :)
                    connection.inputStream.readAllBytes(maxSize, contentLength)
                } else
                    connection.inputStream.readAllBytes(maxSize)
            }
        } catch (e: FileNotFoundException) {
            return null
        }

        val imageInfo = SimpleImageInfo(imageBytes)

        logger.debug { "Image $url was successfully downloaded! width = ${imageInfo.width}; height = ${imageInfo.height}; mimeType = ${imageInfo.mimeType}"}

        if (imageInfo.width > maxWidth || imageInfo.height > maxHeight) {
            logger.warn { "Image $url exceeds the maximum allowed width/height! ${imageInfo.width} > $maxWidth; ${imageInfo.height} > $maxHeight"}
            throw ImageTooLargeException()
        }

        return ImageIO.read(imageBytes.inputStream())
    }

    suspend fun BufferedImage.toByteArray(formatType: ImageFormatType) = withContext(Dispatchers.IO) {
        val output = ByteArrayOutputStream()
        ImageIO.write(this@toByteArray, formatType.name, output)
        output.toByteArray()
    }

    private sealed class RegexMatch(val match: MatchResult)
    private class DiscordEmoteRegexMatch(match: MatchResult) : RegexMatch(match)
    private class UnicodeEmoteRegexMatch(match: MatchResult) : RegexMatch(match)

    sealed class DrawableSection
    data class DrawableText(val text: String) : DrawableSection()
    data class DrawableDiscordEmote(val emoteId: Long, val animated: Boolean) : DrawableSection()
    data class DrawableUnicodeEmote(val emoji: String) : DrawableSection()

    enum class DrawableType {
        TEXT,
        DISCORD_EMOJI,
        UNICODE_EMOJI
    }
}