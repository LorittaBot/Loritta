package net.perfectdreams.loritta.morenitta.commands.vanilla.utils

import net.dv8tion.jda.api.EmbedBuilder
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.utils.*
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage

class ColorInfoCommand(loritta: LorittaBot) : AbstractCommand(loritta, "colorinfo", listOf("rgb", "hexcolor", "hex", "colorpick", "colorpicker"), net.perfectdreams.loritta.common.commands.CommandCategory.UTILS) {
	companion object {
		const val FACTOR = 0.7
		private const val LOCALE_PREFIX = "commands.command.colorinfo"
	}

	override fun getDescriptionKey() = LocaleKeyData("$LOCALE_PREFIX.description")
	override fun getExamplesKey() = LocaleKeyData("$LOCALE_PREFIX.examples")

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			OutdatedCommandUtils.sendOutdatedCommandMessage(context, locale, "colorinfo")

			val input = context.args.joinToString(" ")
			val color = net.perfectdreams.loritta.common.utils.ColorUtils.getColorFromString(input)

			if (color == null) { // Cor inválida!
				context.reply(
                        LorittaReply(
                                message = context.locale["commands.invalidColor", input.stripCodeMarks()],
                                prefix = Emotes.LORI_HM
                        )
				)
				return
			}

			fun Graphics.drawWithOutline(text: String, x: Int, y: Int) {
				this.color = Color.BLACK
				this.drawText(loritta, text, x - 1, y)
				this.drawText(loritta, text, x + 1, y)
				this.drawText(loritta, text, x, y - 1)
				this.drawText(loritta, text, x, y + 1)
				this.color = Color.WHITE
				this.drawText(loritta, text, x, y)
			}

			fun Graphics.drawColor(color: Color, x: Int, y: Int) {
				this.color = color
				this.fillRect(x, y, 48, 48)

				val hex = String.format("#%02x%02x%02x", color.red, color.green, color.blue)

				var _x = x + 48

				for (char in hex) {
					_x -= this.fontMetrics.charWidth(char)
				}

				this.drawWithOutline(hex, _x - 1, y + 48 - 2)
			}

			val colorInfo = BufferedImage(333, 250, BufferedImage.TYPE_INT_ARGB)
			val graphics = colorInfo.graphics

			val volter9 = Constants.VOLTER.deriveFont(9f)

			graphics.font = volter9

			val hsbVals = FloatArray(3)
			Color.RGBtoHSB(color.red, color.green, color.blue, hsbVals)

			val hue = hsbVals[0] * 360
			val saturation = hsbVals[1] * 100
			val value = hsbVals[2] * 100

			val complementaryColor = Color(
					Color.HSBtoRGB(((hue + 180) % 360 / 360), saturation / 100, value / 100)
			)

			val triadColor1 = Color(
					Color.HSBtoRGB(((hue + 120) % 360 / 360), saturation / 100, value / 100)
			)
			val triadColor2 = Color(
					Color.HSBtoRGB(((hue - 120) % 360 / 360), saturation / 100, value / 100)
			)

			val analogousColor1 = Color(
					Color.HSBtoRGB(((hue + 30) % 360 / 360), saturation / 100, value / 100)
			)
			val analogousColor2 = Color(
					Color.HSBtoRGB(((hue - 30) % 360 / 360), saturation / 100, value / 100)
			)

			graphics.drawWithOutline(context.locale["$LOCALE_PREFIX.shades"], 2, 11)

			run {
				var shade = Color(color.rgb)
				var previousShade: Int? = null
				var x = 0

				while (previousShade != shade.rgb) {
					graphics.color = shade
					graphics.drawColor(shade, x, 13)

					val newR = shade.red * (1 - FACTOR)
					val newG = shade.green * (1 - FACTOR)
					val newB = shade.blue * (1 - FACTOR)

					previousShade = shade.rgb
					shade = Color(newR.toInt(), newG.toInt(), newB.toInt())
					x += 48
				}
			}

			graphics.drawWithOutline(context.locale["$LOCALE_PREFIX.tints"], 2, 13 + 48 + 9)

			run {
				var tint = Color(color.rgb)
				var previousTint: Int? = null
				var x = 0

				while (previousTint != tint.rgb) {
					graphics.color = tint
					graphics.drawColor(tint, x, 13 + 48 + 9 + 2)

					val newR = tint.red + (255 - tint.red) * FACTOR
					val newG = tint.green + (255 - tint.green) * FACTOR
					val newB = tint.blue + (255 - tint.blue) * FACTOR

					previousTint = tint.rgb
					tint = Color(newR.toInt(), newG.toInt(), newB.toInt())
					x += 48
				}
			}

			graphics.drawWithOutline(context.locale["$LOCALE_PREFIX.triadic"], 2, 13 + 48 + 9 + 48 + 11)

			graphics.drawColor(color, 0, 13 + 48 + 9 + 48 + 11 + 4)
			graphics.drawColor(triadColor1, 48, 13 + 48 + 9 + 48 + 11 + 4)
			graphics.drawColor(triadColor2, 96, 13 + 48 + 9 + 48 + 11 + 4)

			graphics.drawWithOutline(context.locale["$LOCALE_PREFIX.analogous"], 2, 13 + 48 + 9 + 48 + 11 + 48 + 11 + 3)

			graphics.drawColor(analogousColor1, 0, 13 + 48 + 9 + 48 + 11 + 48 + 15  + 3)
			graphics.drawColor(analogousColor2, 48, 13 + 48 + 9 + 48 + 11 + 48 + 15  + 3)

			graphics.drawWithOutline(context.locale["$LOCALE_PREFIX.complementary"], 146, 13 + 48 + 9 + 48 + 11)

			graphics.drawColor(color, 146, 13 + 48 + 9 + 48 + 11 + 4)
			graphics.drawColor(complementaryColor, 194, 13 + 48 + 9 + 48 + 11 + 4)

			val colorPreview = BufferedImage(192, 192, BufferedImage.TYPE_INT_ARGB)
			val previewGraphics = colorPreview.graphics
			previewGraphics.color = color
			previewGraphics.fillRect(0, 0, 192, 192)

			graphics.drawImage(colorPreview.makeRoundedCorners(99999), 237, 167, null)

			val embed = EmbedBuilder().apply {
				setTitle("\uD83C\uDFA8 ${ColorUtils.getColorNameFromColor(color)}")

				setImage("attachment://color.png")
				setColor(color)

				Color.RGBtoHSB(color.red, color.green, color.blue, hsbVals)

				val hue = (hsbVals[0] * 360).toInt()
				val saturation = (hsbVals[1] * 100).toInt()
				val value = (hsbVals[2] * 100).toInt()

				addField("RGB", "`${color.red}, ${color.green}, ${color.blue}`", true)
				val hex = String.format("#%02x%02x%02x", color.red, color.green, color.blue)
				addField("Hexadecimal", "`$hex`", true)
				addField("Decimal", "`${color.rgb}`", true)
				addField("HSV", "`${hue}°, ${saturation}°, ${value}°`", true)
			}

			context.sendFile(colorInfo, "color.png", embed.build())
		} else {
			context.explain()
		}
	}
}