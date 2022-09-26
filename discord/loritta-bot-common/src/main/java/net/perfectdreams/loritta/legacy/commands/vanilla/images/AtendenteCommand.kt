package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.Loritta
import net.perfectdreams.loritta.legacy.utils.ImageUtils
import net.perfectdreams.loritta.common.utils.LorittaImage
import net.perfectdreams.loritta.legacy.utils.enableFontAntiAliasing
import net.perfectdreams.loritta.common.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.api.commands.arguments
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.legacy.utils.OutdatedCommandUtils
import net.perfectdreams.loritta.legacy.utils.extensions.readImage
import java.awt.Color
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File

class AtendenteCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("atendente"), net.perfectdreams.loritta.common.commands.CommandCategory.IMAGES) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command.atendente"
    }

    override fun command() = create {
        needsToUploadFiles = true

        localizedDescription("$LOCALE_PREFIX.description")
        localizedExamples("$LOCALE_PREFIX.examples")

        usage {
            arguments {
                argument(ArgumentType.TEXT) {}
            }
        }

        executesDiscord {
            val context = this

            if (args.isNotEmpty()) {
                OutdatedCommandUtils.sendOutdatedCommandMessage(this, this.locale, "drawnmask atendente")

                val template = readImage(File(Loritta.ASSETS, "atendente.png"))

                val width = 214
                val height = 131

                val text = args.joinToString(" ")

                val templateGraphics = template.graphics

                val image = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
                val graphics = image.graphics.enableFontAntiAliasing()

                val font = ArtsyJoyLoriConstants.KOMIKA.deriveFont(16f)

                graphics.font = font
                graphics.color = Color.BLACK
                val fontMetrics = graphics.fontMetrics

                val lines = mutableListOf<String>()

                val split = text.split(" ")

                var x = 0
                var currentLine = StringBuilder()

                for (string in split) {
                    val stringWidth = fontMetrics.stringWidth("$string ")
                    val newX = x + stringWidth

                    if (newX >= width) {
                        var endResult = currentLine.toString().trim()
                        if (endResult.isEmpty()) {
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
                var y = (height / 2) - ((skipHeight - 10) * (lines.size - 1))
                for (line in lines) {
                    ImageUtils.drawCenteredStringEmoji(graphics, line, Rectangle(0, y, width, 24), font)
                    y += skipHeight
                }

                val loriImage = LorittaImage(image)
                loriImage.rotate(8.0)
                templateGraphics.drawImage(loriImage.bufferedImage, 46, -20, null)

                context.sendImage(JVMImage(template), "atendente.png", context.getUserMention(true))
            } else {
                context.explain()
            }
        }
    }
}
