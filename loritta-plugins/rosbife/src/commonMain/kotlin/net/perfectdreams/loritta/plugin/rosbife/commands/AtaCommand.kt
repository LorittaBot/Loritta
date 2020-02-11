package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot

object AtaCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					107F, 0F,
					300F, 0F,
					300F, 177F,
					96F, 138F
			)
	)
	override val sourceTemplatePath = "ata.png"
	override val descriptionKey = "commands.images.ata.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("ata")) {}
}