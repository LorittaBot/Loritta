package com.mrpowergamerbr.loritta.commands.vanilla.minecraft

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.utils.remove
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.utils.extensions.readImage
import java.awt.Color
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

			val template = readImage(File(Loritta.ASSETS + "mcconquista.png")) // Template

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
}