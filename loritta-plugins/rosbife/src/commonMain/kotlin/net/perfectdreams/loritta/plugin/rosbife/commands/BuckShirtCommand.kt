package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot

object BuckShirtCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					47f, 90f,
					83f, 91f,
					86f, 133f,
					52f, 133f
			),
			BasicSkewedImageCommand.Corners(
					59f, 209f,
					79f, 210f,
					80f, 233f,
					60f, 234f
			),
			BasicSkewedImageCommand.Corners(
					226f, 105f,
					335f, 113f,
					306f, 236f,
					193f, 218f
			)
	)
	override val sourceTemplatePath = "buckshirt.png"
	override val descriptionKey = "commands.images.buckshirt.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("buckshirt", "buckcamisa")) {}
}