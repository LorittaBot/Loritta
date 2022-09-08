package net.perfectdreams.loritta.cinnamon.discord.utils.images

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import java.awt.Graphics2D
import java.awt.Image
import java.awt.RenderingHints
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream
import java.net.URL
import javax.imageio.ImageIO
import javax.imageio.stream.ImageInputStream

suspend fun readImage(file: File) = withContext(Dispatchers.IO) { ImageIO.read(file) }
suspend fun readImage(url: URL) = withContext(Dispatchers.IO) { ImageIO.read(url) }
suspend fun readImageFromResources(name: String) = readImage(LorittaCinnamon::class.java.getResourceAsStream(name) ?: error("Resource at \"$name\" does not exist!"))
suspend fun readImage(stream: InputStream) = withContext(Dispatchers.IO) { ImageIO.read(stream) }
suspend fun readImage(stream: ImageInputStream) = withContext(Dispatchers.IO) { ImageIO.read(stream) }

fun Graphics2D.withTextAntialiasing(): Graphics2D {
    this.setRenderingHint(
        RenderingHints.KEY_TEXT_ANTIALIASING,
        RenderingHints.VALUE_TEXT_ANTIALIAS_ON
    )
    return this
}

/**
 * Converts a given Image into a BufferedImage
 *
 * @param img The Image to be converted
 * @return The converted BufferedImage
 */
fun Image.toBufferedImage() = ImageUtils.toBufferedImage(this)

/**
 * Resizes an image, this is way faster than using [Image.getScaledInstance], see [https://stackoverflow.com/a/32278737/7271796](https://stackoverflow.com/a/32278737/7271796)
 */
fun Image.getResizedInstance(width: Int, height: Int, interpolationHint: InterpolationType): BufferedImage {
    val newImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
    val graphics = newImage.createGraphics()
    graphics.setRenderingHint(
        RenderingHints.KEY_INTERPOLATION,
        interpolationHint.graphics2DRenderingHint
    )
    graphics.drawImage(this, 0, 0, width, height, null)
    return newImage
}

enum class InterpolationType(val graphics2DRenderingHint: Any) {
    NEAREST_NEIGHBOR(RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR),
    BILINEAR(RenderingHints.VALUE_INTERPOLATION_BILINEAR),
    BICUBIC(RenderingHints.VALUE_INTERPOLATION_BICUBIC)
}