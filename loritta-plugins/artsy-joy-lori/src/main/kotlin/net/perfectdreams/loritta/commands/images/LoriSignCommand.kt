package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class LoriSignCommand : BasicSkewedImageCommand(
		arrayOf("lorisign", "lorittasign", "loriplaca", "lorittaplaca"),
		CommandCategory.IMAGES,
		"commands.images.lorisign.description",
		"loritta_placa.png",
		Corners(
				20f, 202f,

				155f, 226f,

				139f, 299f,

				3f, 275f
		)
)