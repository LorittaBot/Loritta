package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class RomeroBrittoCommand : BasicSkewedImageCommand(
		arrayOf("romerobritto", "pintura", "painting"),
		CommandCategory.IMAGES,
		"commands.images.romerobritto.description",
		"romero_britto.png",
		Corners(
				16F,19F,

				201F,34F,

				208F,218F,

				52F, 294F
		)
)