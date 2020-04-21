package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

object BolsoFrameCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					314F, 36F,
					394F, 41F,
					385F, 156F,
					301F, 151F
			)
	)
	override val sourceTemplatePath = "bolsoframe.png"
	override val descriptionKey = "commands.images.bolsoframe.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("bolsoframe", "bolsonaroframe", "bolsoquadro", "bolsonaroquadro")) {}
}