package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot

object SustoCommand : BasicScaledImageCommand {
	override val descriptionKey = "commands.images.susto.description"
	override val scaleXTo = 84
	override val scaleYTo = 63
	override val x = 61
	override val y = 138
	override val sourceTemplatePath = "loritta_susto.png"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("scared", "fright", "susto")) {}
}