package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicScaledImageCommand

object StudiopolisTvCommand : BasicScaledImageCommand {
	override val descriptionKey = "commands.images.studiopolistv.description"
	override val scaleXTo = 190
	override val scaleYTo = 115
	override val x = 154
	override val y = 61
	override val sourceTemplatePath = "studiopolis.png"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("studiopolis")) {}
}