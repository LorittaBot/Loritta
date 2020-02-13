package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

object BriggsCoverCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					242F,67F, // UL
					381F,88F, // UR
					366F,266F, // LR
					218F, 248F // LL
			)
	)
	override val sourceTemplatePath = "briggs_capa.png"
	override val descriptionKey = "commands.images.briggscover.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("briggscover", "coverbriggs", "capabriggs", "briggscapa")) {}
}