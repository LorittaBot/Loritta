package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.api.LorittaBot

object ArtCommand : BasicSkewedImageCommand {
	override val corners = listOf(
			BasicSkewedImageCommand.Corners(
					75f, 215f,
					172f, 242f,
					106f, 399f,
					13f, 369f
			)
	)
	override val sourceTemplatePath = "art.png"
	override val descriptionKey = "commands.images.art.description"

	override fun command(loritta: LorittaBot) = create(loritta, listOf("art", "arte")) {}
}