package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class LoriAtaCommand : BasicSkewedImageCommand(
		arrayOf("loriata"),
		CommandCategory.IMAGES,
		"commands.images.loriata.description",
		"loriata.png",
		Corners(
				273F, 0F,

				768F, 0F,

				768F, 454F,

				245F, 354F
		)
)