package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

object BolsonaroCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					108F,11F,
					383F,8F,
					375F,167F,
					106F, 158F
			)
	)
	override val sourceTemplatePath = "bolsonaro_tv.png"
	override val descriptionKey = "commands.images.bolsonaro.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("bolsonaro", "bolsonarotv")) {}
}