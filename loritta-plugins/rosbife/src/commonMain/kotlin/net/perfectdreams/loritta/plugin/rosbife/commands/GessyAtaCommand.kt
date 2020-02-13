package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

object GessyAtaCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					130F, 35F,
					410F, 92F,
					387F, 277F,
					111F, 210F
			)
	)
	override val sourceTemplatePath = "gessyata.png"
	override val descriptionKey = "commands.images.gessyata.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("gessyata")) {}
}