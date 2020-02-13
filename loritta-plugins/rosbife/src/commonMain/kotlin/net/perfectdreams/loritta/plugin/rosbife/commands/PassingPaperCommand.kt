package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

object PassingPaperCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					220f, 210f,
					318f, 245f,
					266f, 335f,
					174f, 283f
			)
	)
	override val sourceTemplatePath = "passingpaper.png"
	override val descriptionKey = "commands.images.passingpaper.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("passingpaper", "bilhete", "quizkid")) {}
}