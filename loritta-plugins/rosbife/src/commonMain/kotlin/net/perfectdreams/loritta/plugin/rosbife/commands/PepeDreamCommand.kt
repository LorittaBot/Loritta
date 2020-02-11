package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot

object PepeDreamCommand : BasicScaledImageCommand {
	override val descriptionKey = "commands.images.pepedream.description"
	override val scaleXTo = 100
	override val scaleYTo = 100
	override val x = 81
	override val y = 2
	override val sourceTemplatePath = "pepedream.png"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("pepedream", "sonhopepe", "pepesonho")) {}
}