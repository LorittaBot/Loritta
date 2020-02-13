package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.DrakeBaseCommand

object DrakeCommand : DrakeBaseCommand {
	override val descriptionKey = "commands.images.drake.description"
	override val sourceTemplatePath = "drake.png"

	override fun command(loritta: LorittaBot) = create(
			loritta,
			listOf("drake")
	)
}