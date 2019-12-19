package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class CoringaCommand : BasicSkewedImageCommand(
		arrayOf("coringa", "joker"),
		CommandCategory.IMAGES,
		"commands.images.coringa.description",
		"coringa.png",
		Corners(
				255f, 196f,
				399f, 206f,
				388f, 297f,
				251f, 294f
		)
)