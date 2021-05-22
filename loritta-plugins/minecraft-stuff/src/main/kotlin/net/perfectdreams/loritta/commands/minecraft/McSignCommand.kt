package net.perfectdreams.loritta.commands.minecraft

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.MinecraftStuff
import net.perfectdreams.loritta.api.commands.ArgumentType
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.legacy.commands.DiscordAbstractCommandBase
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Rectangle
import java.io.File

class McSignCommand(val m: MinecraftStuff) : DiscordAbstractCommandBase(m.loritta, listOf("mcsign", "mcplaca"), CommandCategory.MINECRAFT) {
	override fun command() = create {
		localizedDescription("commands.command.mcsign.description")
		localizedExamples("commands.command.mcsign.examples")

		usage {
			argument(ArgumentType.TEXT) {}
		}

		executes {
			if (args.isNotEmpty()) {
				var lines = args.joinToString(" ").split("|")

				var signType = SignType.OAK
				val customSignType = SignType.values().firstOrNull {
					it.localizedNames.any {
						localizedName -> lines.first().startsWith(localizedName, true)
					}
				}
				if (customSignType != null) {
					signType = customSignType
					lines = lines.drop(1)
				}

				lines.forEach { it.trim() }
				val template = readImage(File(Loritta.ASSETS + "sign_${signType.name.toLowerCase()}.png")) // Template

				val graphics = template.graphics

				val minecraftia = Constants.MINECRAFTIA
						.deriveFont(17f) // A fonte para colocar na placa

				graphics.font = minecraftia
				graphics.color = Color(0, 0, 0)
				var currentY = 2

				for (i in 0 until Math.min(4, lines.size)) {
					drawCenteredStringColored(graphics, lines[i], Rectangle(0, currentY, 192, 23), minecraftia)
					currentY += 23
				}

				sendImage(JVMImage(template), "sign.png")
			} else {
				explain()
			}
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
		val colored = text.replace("&", "§")
		val stripped = colored.replace(Regex("(?i)§[0-9A-FK-OR]"), "")
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