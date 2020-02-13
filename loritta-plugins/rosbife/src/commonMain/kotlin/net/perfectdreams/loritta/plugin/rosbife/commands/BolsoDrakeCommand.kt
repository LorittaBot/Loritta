package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.DrakeBaseCommand

object BolsoDrakeCommand : DrakeBaseCommand {
	override val descriptionKey = "commands.images.bolsodrake.description"
	override val sourceTemplatePath = "bolsodrake.png"

	override fun command(loritta: LorittaBot) = create(
			loritta,
			listOf("bolsodrake")
	)
}