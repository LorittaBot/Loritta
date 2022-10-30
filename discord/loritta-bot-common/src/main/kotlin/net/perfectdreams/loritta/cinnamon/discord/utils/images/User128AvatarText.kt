package net.perfectdreams.loritta.cinnamon.discord.utils.images

import dev.kord.core.entity.User
import net.perfectdreams.loritta.morenitta.LorittaBot
import java.awt.Color
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.image.BufferedImage

object User128AvatarText {
    suspend fun draw(
        loritta: LorittaBot,
        image: BufferedImage,
        x: Int,
        y: Int,
        user: User,
        avatar: BufferedImage,
        text: String,
        fontColor: Color,
    ) {
        val graphics = image.createGraphics().withTextAntialiasing()

        graphics.drawImage(avatar.getResizedInstance(128, 128, InterpolationType.BILINEAR), x, y, null)

        graphics.font = loritta.graphicsFonts.m5x7.deriveFont(16f)
        graphics.color = Color.BLACK
        val stringYBase = y + 10
        val stringXBase = x + 1

        // Outline
        for (xPlus in -1..1) {
            for (yPlus in -1..1) {
                graphics.drawString(user.tag, stringXBase + xPlus, stringYBase + yPlus)
            }
        }

        graphics.color = Color.WHITE
        // Text
        graphics.drawString(user.tag, stringXBase, stringYBase)

        graphics.font = loritta.graphicsFonts.bebasNeueRegular.deriveFont(22f)

        drawCentralizedTextOutlined(
            loritta,
            graphics,
            text,
            Rectangle(x, y + 80, 128, 42),
            fontColor,
            Color.BLACK,
            2
        )

        graphics.dispose()
    }

    private suspend fun drawCentralizedTextOutlined(
        loritta: LorittaBot,
        graphics: Graphics,
        text: String,
        rectangle: Rectangle,
        fontColor: Color,
        strokeColor: Color,
        strokeSize: Int
    ) {
        val font = graphics.font
        graphics.font = font
        val fontMetrics = graphics.fontMetrics

        val lines = mutableListOf<String>()

        val split = text.split(" ")

        var x = 0
        var currentLine = StringBuilder()

        for (string in split) {
            val stringWidth = fontMetrics.stringWidth("$string ")
            val newX = x + stringWidth

            if (newX >= rectangle.width) {
                var endResult = currentLine.toString().trim()
                if (endResult.isEmpty()) { // okay wtf
                    // Se o texto é grande demais e o conteúdo atual está vazio... bem... substitua o endResult pela string atual
                    endResult = string
                    lines.add(endResult)
                    x = 0
                    continue
                }
                lines.add(endResult)
                currentLine = StringBuilder()
                currentLine.append(' ')
                currentLine.append(string)
                x = fontMetrics.stringWidth("$string ")
            } else {
                currentLine.append(' ')
                currentLine.append(string)
                x = newX
            }
        }
        lines.add(currentLine.toString().trim())

        val skipHeight = fontMetrics.ascent
        var y = (rectangle.height / 2) - ((skipHeight - 4) * (lines.size - 1))
        for (line in lines) {
            graphics.color = strokeColor
            for (strokeX in rectangle.x - strokeSize..rectangle.x + strokeSize) {
                for (strokeY in rectangle.y + y - strokeSize..rectangle.y + y + strokeSize) {
                    ImageUtils.drawCenteredString(
                        loritta,
                        graphics,
                        line,
                        Rectangle(strokeX, strokeY, rectangle.width, 24),
                        allowedDrawableTypes = listOf(ImageUtils.DrawableType.TEXT)
                    )
                }
            }
            graphics.color = fontColor
            ImageUtils.drawCenteredString(
                loritta,
                graphics,
                line,
                Rectangle(rectangle.x, rectangle.y + y, rectangle.width, 24),
                allowedDrawableTypes = listOf(ImageUtils.DrawableType.TEXT)
            )
            y += skipHeight
        }
    }
}