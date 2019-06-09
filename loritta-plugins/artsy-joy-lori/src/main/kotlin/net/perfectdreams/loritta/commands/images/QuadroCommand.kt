package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class QuadroCommand : BasicSkewedImageCommand(
		arrayOf("quadro", "frame", "picture"),
		CommandCategory.IMAGES,
		"commands.images.wolverine.description",
		"wolverine.png",
		Corners(
				55F, 165F,

				152F, 159F,

				172F, 283F,

				73F, 293F
		)
)