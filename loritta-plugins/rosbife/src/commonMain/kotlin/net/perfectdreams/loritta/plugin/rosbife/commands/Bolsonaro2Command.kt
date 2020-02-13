package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

object Bolsonaro2Command : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					213F,34F,
					435F,40F,
					430F,166F,
					212F, 161F
			)
	)
	override val sourceTemplatePath = "bolsonaro_tv2.png"
	override val descriptionKey = "commands.images.bolsonaro.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("bolsonaro2", "bolsonarotv2")) {}
}