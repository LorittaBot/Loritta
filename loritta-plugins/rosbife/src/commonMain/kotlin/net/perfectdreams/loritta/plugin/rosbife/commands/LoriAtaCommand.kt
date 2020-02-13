package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

object LoriAtaCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					273F, 0F,
					768F, 0F,
					768F, 454F,
					245F, 354F
			)
	)
	override val sourceTemplatePath = "loriata.png"
	override val descriptionKey = "commands.images.loriata.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("loriata")) {}
}