package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class SustoCommand : BasicScaledImageCommand(
		arrayOf("scared", "fright", "susto"),
		CommandCategory.IMAGES,
		"commands.images.susto.description",
		"loritta_susto.png",
		84, 63,
		61, 138
)