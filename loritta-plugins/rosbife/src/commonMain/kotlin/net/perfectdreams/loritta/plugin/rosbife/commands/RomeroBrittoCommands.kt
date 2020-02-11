package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot

object RomeroBrittoCommands : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					16F,19F,
					201F,34F,
					208F,218F,
					52F, 294F
			)
	)
	override val sourceTemplatePath = "romero_britto.png"
	override val descriptionKey = "commands.images.romerobritto.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("romerobritto", "pintura", "painting")) {}
}