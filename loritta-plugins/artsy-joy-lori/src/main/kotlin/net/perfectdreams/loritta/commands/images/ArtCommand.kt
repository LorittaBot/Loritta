package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class ArtCommand : BasicSkewedImageCommand(
		arrayOf("art", "arte"),
		CommandCategory.IMAGES,
		"commands.images.art.description",
		"art.png",
		Corners(
				75f, 215f,

				172f, 242f,

				106f, 399f,

				13f, 369f
		)
)