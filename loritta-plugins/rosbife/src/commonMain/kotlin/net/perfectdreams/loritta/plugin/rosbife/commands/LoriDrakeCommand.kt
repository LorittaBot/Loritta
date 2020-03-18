package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.DrakeBaseCommand

object LoriDrakeCommand : DrakeBaseCommand {
	override val descriptionKey = "commands.images.loridrake.description"
	override val sourceTemplatePath = "lori_drake.png"
	override val scale = 2

	override fun command(loritta: LorittaBot) = create(
			loritta,
			listOf("loridrake")
	)
}