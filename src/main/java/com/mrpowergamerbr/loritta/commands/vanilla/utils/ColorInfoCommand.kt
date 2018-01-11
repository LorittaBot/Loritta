package com.mrpowergamerbr.loritta.commands.vanilla.utils

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.ColorUtils
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.drawText
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.makeRoundedCorners
import net.dv8tion.jda.core.EmbedBuilder
import java.awt.Color
import java.awt.Graphics
import java.awt.image.BufferedImage

class ColorInfoCommand : AbstractCommand("colorinfo", listOf("rgb", "hexcolor", "hex", "colorpick", "colorpicker"), CommandCategory.UTILS) {
	companion object {
		val HEX_PATTERN = "#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})".toPattern()
		val RGB_PATTERN = "(\\d{1,3})(?:,| |, )(\\d{1,3})(?:,| |, )(\\d{1,3})(?:(?:,| |, )(\\d{1,3}))?".toPattern()
		val COLOR_UTILS = ColorUtils()
		val FACTOR = 0.7
	}

	override fun getDescription(locale: BaseLocale): String {
		return locale["COLOR_Description"]
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val input = context.args.joinToString(" ")
			var color: Color? = null

			val hexMatcher = ColorInfoCommand.HEX_PATTERN.matcher(input)
			val rgbMatcher = ColorInfoCommand.RGB_PATTERN.matcher(input)

			if (hexMatcher.find()) { // Hexadecimal
				color = Color.decode("#" + hexMatcher.group(1))
			}

			if (rgbMatcher.find()) { // RGB
				var r = rgbMatcher.group(1).toInt()
				var g = rgbMatcher.group(2).toInt()
				var b = rgbMatcher.group(3).toInt()

				color = Color(r, g, b)
			}

			var packedInt = input.toIntOrNull()

			if (packedInt != null) { // Packed Int
				color = Color(packedInt)
			}

			if (color == null) { // Cor inválida!
				context.reply(
						LoriReply(
								message = locale["COLOR_InvalidColor", input],
								prefix = Constants.ERROR
						)
				)
				return
			}

			fun Graphics.drawWithOutline(text: String, x: Int, y: Int) {
				this.color = Color.BLACK
				this.drawText(text, x - 1, y)
				this.drawText(text, x + 1, y)
				this.drawText(text, x, y - 1)
				this.drawText(text, x, y + 1)
				this.color = Color.WHITE
				this.drawText(text, x, y)
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

			graphics.drawWithOutline(locale["COLOR_Shades"], 2, 11)

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

			graphics.drawWithOutline(locale["COLOR_Tints"], 2, 13 + 48 + 9)

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

			graphics.drawWithOutline(locale["COLOR_Triadic"], 2, 13 + 48 + 9 + 48 + 11)

			graphics.drawColor(color, 0, 13 + 48 + 9 + 48 + 11 + 4)
			graphics.drawColor(triadColor1, 48, 13 + 48 + 9 + 48 + 11 + 4)
			graphics.drawColor(triadColor2, 96, 13 + 48 + 9 + 48 + 11 + 4)

			graphics.drawWithOutline(locale["COLOR_Analogous"], 2, 13 + 48 + 9 + 48 + 11 + 48 + 11 + 3)

			graphics.drawColor(analogousColor1, 0, 13 + 48 + 9 + 48 + 11 + 48 + 15  + 3)
			graphics.drawColor(analogousColor2, 48, 13 + 48 + 9 + 48 + 11 + 48 + 15  + 3)

			graphics.drawWithOutline(locale["COLOR_Complementary"], 146, 13 + 48 + 9 + 48 + 11)

			graphics.drawColor(color, 146, 13 + 48 + 9 + 48 + 11 + 4)
			graphics.drawColor(complementaryColor, 194, 13 + 48 + 9 + 48 + 11 + 4)

			val colorPreview = BufferedImage(192, 192, BufferedImage.TYPE_INT_ARGB)
			val previewGraphics = colorPreview.graphics
			previewGraphics.color = color
			previewGraphics.fillRect(0, 0, 192, 192)

			graphics.drawImage(colorPreview.makeRoundedCorners(99999), 237, 167, null)

			val embed = EmbedBuilder().apply {
				setTitle("\uD83C\uDFA8 ${COLOR_UTILS.getColorNameFromColor(color)}")

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