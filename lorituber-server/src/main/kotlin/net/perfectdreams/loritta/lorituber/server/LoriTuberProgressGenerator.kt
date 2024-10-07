package net.perfectdreams.loritta.lorituber.server

import java.awt.*
import java.awt.geom.AffineTransform
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

fun main() {
    repeat(5) {
        val image = BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB)

        val graphics = image.createGraphics()

        graphics.color = Color.decode("#1e1f22")

        graphics.fillRect(0, 0, 256, 256)

        // Red motive
        // graphics.color = Color.decode("#dd2e44")
        // graphics.color = Color.decode("#78b159")

        val gradient = GradientPaint(
            0f, 0f, Color.decode("#78b159"),  // Start point (0,0) and color red
            0f, 256f, Color.decode("#65964B")  // End point (width,height) and color blue
        )

        // Set the paint to the gradient
        graphics.paint = gradient

        var value = 0
        repeat(it) {
            value += 64
        }

        graphics.fillRect(0, 0, value, 256)

        ImageIO.write(image, "png", File("progress_green_$it.png"))
    }

    repeat(5) {
        val image = BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB)

        val graphics = image.createGraphics()

        graphics.color = Color.decode("#1e1f22")

        graphics.fillRect(0, 0, 256, 256)

        // Red motive
        // graphics.color = Color.decode("#dd2e44")
        // graphics.color = Color.decode("#78b159")

        val gradient = GradientPaint(
            0f, 0f, Color.decode("#DA373C"),  // Start point (0,0) and color red
            0f, 256f, Color.decode("#BF3035")  // End point (width,height) and color blue
        )

        // Set the paint to the gradient
        graphics.paint = gradient

        var value = 0
        repeat(it) {
            value += 64
        }

        graphics.fillRect(0, 0, value, 256)

        ImageIO.write(image, "png", File("progress_red_$it.png"))
    }

    // Motives triangle
    val padHorizontal = 40
    val padVertical = 24
    run {
        val image = BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB)

        val graphics = image.createGraphics()
        graphics.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )
        graphics.color = Color.decode("#1e1f22")

        // graphics.fillRect(0, 0, 256, 256)

        // Red motive
        // graphics.color = Color.decode("#dd2e44")
        // graphics.color = Color.decode("#78b159")

        val gradient = GradientPaint(
            0f, 0f, Color.decode("#78b159"),  // Start point (0,0) and color red
            0f, 256f, Color.decode("#65964B")  // End point (width,height) and color blue
        )

        // Set the paint to the gradient
        graphics.paint = gradient

        // Define the points of the triangle
        // the -16 is padding
        val xPoints = intArrayOf(128 - padHorizontal, 256 - padHorizontal, 256 - padHorizontal)
        val yPoints = intArrayOf(128, 256 - padVertical, 0 + padVertical)
        val nPoints = 3

        // Create a Polygon object for the triangle
        val triangle = Polygon(xPoints, yPoints, nPoints)

        // Optionally, fill the triangle
        graphics.fillPolygon(triangle)

        ImageIO.write(flipImageHorizontally(image), "png", File("motive_bar_positive_arrow_1.png"))
    }

    run {
        val image = BufferedImage(256, 256, BufferedImage.TYPE_INT_ARGB)

        val graphics = image.createGraphics()
        graphics.setRenderingHint(
            RenderingHints.KEY_ANTIALIASING,
            RenderingHints.VALUE_ANTIALIAS_ON
        )
        graphics.color = Color.decode("#1e1f22")

        // graphics.fillRect(0, 0, 256, 256)

        // Red motive
        // graphics.color = Color.decode("#dd2e44")
        // graphics.color = Color.decode("#78b159")

        val gradient = GradientPaint(
            0f, 0f, Color.decode("#DA373C"),  // Start point (0,0) and color red
            0f, 256f, Color.decode("#BF3035")  // End point (width,height) and color blue
        )

        // Set the paint to the gradient
        graphics.paint = gradient

        // Define the points of the triangle
        // the -32 is padding
        val xPoints = intArrayOf(128 - padHorizontal, 256 - padHorizontal, 256 - padHorizontal)
        val yPoints = intArrayOf(128, 256 - padVertical, 0 + padVertical)
        val nPoints = 3

        // Create a Polygon object for the triangle
        val triangle = Polygon(xPoints, yPoints, nPoints)

        // Optionally, fill the triangle
        graphics.fillPolygon(triangle)

        ImageIO.write(image, "png", File("motive_bar_negative_arrow_1.png"))
    }
}

fun flipImageHorizontally(image: BufferedImage): BufferedImage {
    val width = image.width
    val height = image.height

    // Create a new BufferedImage to hold the flipped image
    val flippedImage = BufferedImage(width, height, image.type)

    // Get the Graphics2D context of the flipped image
    val g2d: Graphics2D = flippedImage.createGraphics()

    // Set the AffineTransform to flip the image horizontally
    val transform = AffineTransform()
    transform.scale(-1.0, 1.0)  // Flip horizontally
    transform.translate(-width.toDouble(), 0.0)  // Move the image back to the correct position

    // Draw the flipped image using the transform
    g2d.drawImage(image, transform, null)
    g2d.dispose()

    return flippedImage
}