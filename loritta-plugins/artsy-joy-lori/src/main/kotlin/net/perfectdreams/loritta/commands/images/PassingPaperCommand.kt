package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class PassingPaperCommand : BasicSkewedImageCommand(
		arrayOf("passingpaper", "bilhete", "quizkid"),
		CommandCategory.IMAGES,
		"commands.images.passingpaper.description",
		"passingpaper.png",
		Corners(
				220f, 210f,

				318f, 245f,

				266f, 335f,

				174f, 283f
		)
)