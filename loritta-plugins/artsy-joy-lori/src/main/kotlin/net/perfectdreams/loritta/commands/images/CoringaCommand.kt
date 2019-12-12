package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class CoringaCommand : BasicSkewedImageCommand(
		arrayOf("coringa", "joker"),
		CommandCategory.IMAGES,
		"commands.images.coringa.description",
		"coringa.png",
		Corners(
				255.74295043945312f, 196.27969360351562f,
				399.2554931640625f, 206.9936065673828f,
				388.00860595703125f, 304.5141906738281f,
				251.88442993164062f, 294.34588623046875f
		)
)