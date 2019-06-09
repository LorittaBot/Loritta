package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class AtaCommand : BasicSkewedImageCommand(
		arrayOf("ata"),
		CommandCategory.IMAGES,
		"commands.images.ata.description",
		"ata.png",
		Corners(
				107F, 0F,

				300F, 0F,

				300F, 177F,

				96F, 138F
		)
)