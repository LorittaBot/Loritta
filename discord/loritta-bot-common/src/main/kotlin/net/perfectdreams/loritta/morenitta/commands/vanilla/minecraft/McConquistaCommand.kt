package net.perfectdreams.loritta.morenitta.commands.vanilla.minecraft

import net.perfectdreams.loritta.morenitta.Loritta
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.morenitta.utils.remove
import net.perfectdreams.loritta.morenitta.utils.extensions.readImage
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File

class McConquistaCommand : AbstractCommand("mcconquista", listOf("mcprogresso", "mcadvancement", "mcachievement"), net.perfectdreams.loritta.common.commands.CommandCategory.MINECRAFT) {
	override fun getDescriptionKey() = LocaleKeyData("commands.command.mcadvancement.description")
	override fun getExamplesKey() = LocaleKeyData("commands.command.mcadvancement.examples")

	// TODO: Fix Usage

	override fun needsToUploadFiles(): Boolean {
		return true
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		if (context.args.size > 1) {
			val image = context.getImageAt(0) ?: run { Constants.INVALID_IMAGE_REPLY.invoke(context); return; }

			val advancementText = context.rawArgs.remove(0).joinToString(" ")

			val template = readImage(File(Loritta.ASSETS + "mcconquista.png")) // Template

			val graphics = template.graphics

			val minecraftia = Constants.MINECRAFTIA
					.deriveFont(24f) // A fonte para colocar no progresso

			graphics.font = minecraftia
			graphics.color = Color(255, 255, 0)

			graphics.drawString(context.locale["commands.command.mcadvancement.advancementMade"], 90, 41 + 14)
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