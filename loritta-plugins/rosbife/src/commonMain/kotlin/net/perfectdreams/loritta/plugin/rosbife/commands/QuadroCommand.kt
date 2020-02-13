package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

object QuadroCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					55F, 165F,
					152F, 159F,
					172F, 283F,
					73F, 293F
			)
	)
	override val sourceTemplatePath = "wolverine.png"
	override val descriptionKey = "commands.images.wolverine.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("quadro", "frame", "picture")) {}
}