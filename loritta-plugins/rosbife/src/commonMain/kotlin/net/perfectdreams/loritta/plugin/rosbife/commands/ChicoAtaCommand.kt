package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot

object ChicoAtaCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					300F, 0F,
					768F, 0F,
					768F, 480F,
					300F, 383F
			)
	)
	override val sourceTemplatePath = "chicoata.png"
	override val descriptionKey = "commands.images.chicoata.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("chicoata")) {}
}