package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class PepeDreamCommand : BasicScaledImageCommand(
		arrayOf("pepedream", "sonhopepe", "pepesonho"),
		CommandCategory.IMAGES,
		"commands.images.pepedream.description",
		"pepedream.png",
		100, 100,
		81, 2
)