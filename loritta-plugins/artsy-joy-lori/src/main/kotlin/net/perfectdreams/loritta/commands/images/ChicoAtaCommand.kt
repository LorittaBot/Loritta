package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class ChicoAtaCommand : BasicSkewedImageCommand(
		arrayOf("chicoata"),
		CommandCategory.IMAGES,
		"commands.images.chicoata.description",
		"chicoata.png",
		Corners(
				300F, 0F,

				768F, 0F,

				768F, 480F,

				300F, 383F
		)
)