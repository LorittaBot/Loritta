package net.perfectdreams.loritta.commands.images

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.enableFontAntiAliasing
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.commands.arguments
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.Color
import java.awt.FontMetrics
import java.awt.Graphics
import java.awt.image.BufferedImage
import java.io.File

class DrawnWordCommand(loritta: LorittaDiscord) : DiscordAbstractCommandBase(loritta, listOf("drawnword"), CommandCategory.IMAGES) {
    companion object {
        private const val LOCALE_PREFIX = "commands.command"
    }

    override fun command() = create {
        needsToUploadFiles = true

        localizedDescription("$LOCALE_PREFIX.drawnword.description")
        localizedExamples("$LOCALE_PREFIX.drawnword.examples")

        usage {
            arguments {
                argument(ArgumentType.TEXT) {}
            }
        }

        executesDiscord {
            val context = this

            if (args.isEmpty()) explainAndExit()

            val text = args.joinToString(" ").substringIfNeeded(0..800)

            fun getTextWrapSpacesRequiredHeight(text: String, startX: Int, startY: Int, endX: Int, endY: Int, fontMetrics: FontMetrics, graphics: Graphics): Int {
                val lineHeight = fontMetrics.height

                var currentX = startX
                var currentY = startY

                val split = text.split("((?<= )|(?= ))".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                for (str in split) {
                    var width = fontMetrics.stringWidth(str)
                    if (currentX + width > endX) {
                        currentX = startX
                        currentY += lineHeight
                    }
                    var idx = 0
                    for (c in str.toCharArray()) {
                        idx++
                        if (c == '\n') {
                            currentX = startX
                            currentY += lineHeight
                            continue
                        }
                        width = fontMetrics.charWidth(c)
                        if (!graphics.font.canDisplay(c)) {
                            val emoteImage = ImageUtils.getTwitterEmoji(str, idx)
                            if (emoteImage != null) {
                                currentX += width
                            }

                            continue
                        }

                        currentX += width
                    }
                }
                return currentY
            }

            val drawnMaskWordImage = readImage(File(Loritta.ASSETS, "drawn_mask_word.png"))
            val drawnMaskWordBottomImage = readImage(File(Loritta.ASSETS, "drawn_mask_word_bottom.png"))
            val babyMaskChairImage = readImage(File(Loritta.ASSETS, "baby_mask_chair.png"))

            var wordScreenHeight = drawnMaskWordImage.height

            val width = 468

            val graphics = drawnMaskWordImage.graphics.enableFontAntiAliasing()

            val font2 = graphics.font.deriveFont(24f)
            graphics.font = font2
            val fontMetrics = graphics.fontMetrics

            val lineHeight = fontMetrics.height
            val startY = 90

            val currentY = getTextWrapSpacesRequiredHeight(
                    text,
                    54,
                    90,
                    drawnMaskWordImage.width,
                    99999,
                    fontMetrics,
                    graphics
            )

            val currentJumps = (currentY - startY) / lineHeight

            val pixelsNeeded = currentY - startY

            if (currentJumps > 4) {
                val overflownPixels = (pixelsNeeded - (lineHeight * 3)) + lineHeight + lineHeight
                val requiredPastes = (overflownPixels / 53)

                wordScreenHeight += (53 * requiredPastes) - 27
            }

            val wordScreen = BufferedImage(drawnMaskWordImage.width, wordScreenHeight, BufferedImage.TYPE_INT_ARGB)
            val wordScreenGraphics = wordScreen.graphics.enableFontAntiAliasing()

            wordScreenGraphics.drawImage(drawnMaskWordImage, 0, 0, null)


            wordScreenGraphics.color = Color.BLACK
            val font = wordScreenGraphics.font.deriveFont(24f)


            wordScreenGraphics.font = font2
            val fontMetrics2 = wordScreenGraphics.fontMetrics

            if (currentJumps > 4) {
                val overflownPixels = (pixelsNeeded - (lineHeight * 3)) + lineHeight + lineHeight
                val requiredPastes = (overflownPixels / 53)

                var currentY = (drawnMaskWordImage.height - 40)

                repeat(requiredPastes) {
                    wordScreenGraphics.drawImage(drawnMaskWordBottomImage, 0, currentY, null)

                    currentY += 53
                }
            }

            ImageUtils.drawTextWrapSpaces(
                    text,
                    54,
                    90,
                    wordScreen.width,
                    99999,
                    fontMetrics2,
                    wordScreenGraphics
            )

            val image = BufferedImage(width, wordScreen.height + 202, BufferedImage.TYPE_INT_ARGB)
            val imageGraphics = image.graphics

            imageGraphics.fillRect(0, 0, 468, wordScreen.height + 202)
            imageGraphics.drawImage(wordScreen, 218, 0, null)
            imageGraphics.drawImage(babyMaskChairImage, 0, image.height - babyMaskChairImage.height, null)

            context.sendImage(JVMImage(image), "drawn_word.png", context.getUserMention(true))
        }
    }
}