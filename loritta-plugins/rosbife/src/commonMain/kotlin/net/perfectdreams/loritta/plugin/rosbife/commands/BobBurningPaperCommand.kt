package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

object BobBurningPaperCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					21f, 373f,
					14f, 353f,
					48f, 334f,
					82f, 354f
			),
			BasicSkewedImageCommand.Corners(
					24f, 32f,
					138f, 33f,
					137f, 177f,
					20f, 175f
			)
	)
	override val sourceTemplatePath = "bobfire.png"
	override val descriptionKey = "commands.images.bobfire.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("bobburningpaper", "bobpaperfire", "bobfire", "bobpapelfogo", "bobfogo")) {}
}