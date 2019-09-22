package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class GessyAtaCommand : BasicSkewedImageCommand(
		arrayOf("gessyata", "gessoata"),
		CommandCategory.IMAGES,
		"commands.images.gessyata.description",
		"gessyata.png",
		Corners(
				130F, 35F,

				410F, 92F,

				387F, 277F,

				111F, 210F
		)
)