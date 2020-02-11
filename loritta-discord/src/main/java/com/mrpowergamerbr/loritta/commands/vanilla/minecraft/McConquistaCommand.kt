package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import net.perfectdreams.loritta.api.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.remove
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class McConquistaCommand : AbstractCommand("mcconquista", listOf("mcprogresso", "mcadvancement", "mcachievement"), CommandCategory.MINECRAFT) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return locale["MCCONQUISTA_Description"]
	}

	override fun getExamples(): List<String> {
		return listOf("@Loritta Ser muito fofa!")
	}

	override fun getUsage(): String {
		return "texto"
	}

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		if (context.args.size > 1) {
			val image = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

			val advancementText = context.rawArgs.remove(0).joinToString(" ")

			val template = ImageIO.read(File(Loritta.ASSETS + "mcconquista.png")) // Template

			val graphics = template.graphics

			val minecraftia = Constants.MINECRAFTIA
					.deriveFont(24f) // A fonte para colocar no progresso

			graphics.font = minecraftia
			graphics.color = Color(255, 255, 0)

			graphics.drawString(context.legacyLocale["MCCONQUISTA_AdvancementMade"], 90, 41 + 14)
			graphics.color = Color(255, 255, 255)

			var remadeText = ""
			var x = 90
			for (ch in advancementText) {
				if (x + graphics.fontMetrics.charWidth(ch) > 468) {
					remadeText = remadeText.substring(0, remadeText.length - 3) + "..."
					break
				}
				x += graphics.fontMetrics.charWidth(ch)
				remadeText += ch
			}

			graphics.drawString(remadeText, 90, 74 + 14)
			graphics.drawImage(image.getScaledInstance(70, 70, BufferedImage.SCALE_SMOOTH), 16, 14, null)

			context.sendFile(template, "advancement.png", context.getAsMention(true))
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

		var currentX = x
		var nextIsColor = false
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