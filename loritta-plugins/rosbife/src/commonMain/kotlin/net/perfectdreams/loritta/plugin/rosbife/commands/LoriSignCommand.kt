package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

object LoriSignCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					20f, 202f,
					155f, 226f,
					139f, 299f,
					3f, 275f
			)
	)
	override val sourceTemplatePath = "loritta_placa.png"
	override val descriptionKey = "commands.images.lorisign.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("lorisign", "lorittasign", "loriplaca", "lorittaplaca")) {}
}