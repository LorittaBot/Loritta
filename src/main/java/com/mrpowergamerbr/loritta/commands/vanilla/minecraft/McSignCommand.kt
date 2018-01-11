package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Rectangle
import java.io.File
import javax.imageio.ImageIO

class McSignCommand : AbstractCommand("mcsign", category = CommandCategory.MINECRAFT) {
	override fun getDescription(locale: BaseLocale): String {
		return locale["MCSIGN_DESCRIPTION"]
	}

	override fun getExample(): List<String> {
		return listOf("Isto é um texto | em uma placa! | | Legal né?");
	}

	override fun getUsage(): String {
		return "texto";
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty()) {
			val lines = context.args.joinToString(" ").split("|");
			lines.forEach { it.trim() }
			val template = ImageIO.read(File(Loritta.ASSETS + "sign.png")) // Template

			val graphics = template.graphics

			val minecraftia = Constants.MINECRAFTIA
					.deriveFont(17f) // A fonte para colocar na placa

			graphics.font = minecraftia;
			graphics.color = Color(0, 0, 0)
			var currentY = 2;

			for (i in 0 until lines.size) {
				drawCenteredStringColored(graphics, lines[i], Rectangle(0, currentY, 192, 23), minecraftia);
				currentY += 23;
			}

			context.sendFile(template, "placa.png", context.getAsMention(true))
		} else {
			context.explain()
		}
	}

	fun drawCenteredStringColored(graphics: Graphics, text: String, rect: Rectangle, font: Font) {
		val colors = mapOf(
				'0' to Color(0, 0, 0),
				'1' to Color(0, 0, 170),
				'2' to Color(0, 170, 0),
				'3' to Color(0, 170, 170),
				'4' to Color(170, 0, 0),
				'5' to Color(170, 0, 170),
				'6' to Color(255, 170, 0),
				'7' to Color(170, 170, 170),
				'8' to Color(85, 85, 85),
				'9' to Color(85, 85, 255),
				'a' to Color(85, 255, 85),
				'b' to Color(85, 255, 255),
				'c' to Color(255, 85, 85),
				'd' to Color(255, 85, 255),
				'e' to Color(255, 255, 85),
				'f' to Color(255, 255, 255)
		)
		var colored = text.replace("&", "§")
		var stripped = colored.replace(Regex("(?i)§[0-9A-FK-OR]"), "")
		// Get the FontMetrics
		val metrics = graphics.getFontMetrics(font)
		// Determine the X coordinate for the text
		val x = rect.x + (rect.width - metrics.stringWidth(stripped)) / 2
		// Determine the Y coordinate for the text (note we add the ascent, as in java 2d 0 is top of the screen)
		val y = rect.y + (rect.height - metrics.height) / 2 + metrics.ascent

		var currentX = x;
		var nextIsColor = false;
		// Dar um loop em todos os chars da nossa string
		for (char in colored) {
			if (char == '§') { // Controlador de cor!
				nextIsColor = true
				continue
			}
			if (nextIsColor) {
				nextIsColor = false
				if (colors.containsKey(char)) {
					graphics.color = colors[char]
					graphics.font = font
				}
				if (char == 'l') {
					graphics.font = graphics.font.deriveFont(Font.BOLD)
				}
				if (char == 'o') {
					graphics.font = graphics.font.deriveFont(Font.ITALIC)
				}
				continue
			}
			graphics.drawString(char.toString(), currentX, y)
			currentX += metrics.charWidth(char)
		}
		graphics.color = Color(0, 0, 0)
	}
}