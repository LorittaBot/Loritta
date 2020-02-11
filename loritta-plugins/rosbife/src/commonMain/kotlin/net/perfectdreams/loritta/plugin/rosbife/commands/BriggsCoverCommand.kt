package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot

object BriggsCoverCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					108F,11F,
					383F,8F,
					375F,167F,
					106F, 158F
			)
	)
	override val sourceTemplatePath = "briggs_capa.png"
	override val descriptionKey = "commands.images.briggscover.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("briggscover", "coverbriggs", "capabriggs", "briggscapa")) {}
}