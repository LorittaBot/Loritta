package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class Bolsonaro2Command : BasicSkewedImageCommand(
		arrayOf("bolsonaro2", "bolsonarotv2"),
		CommandCategory.IMAGES,
		"commands.images.bolsonaro.description",
		"bolsonaro_tv2.png",
		Corners(
				213F,34F,

				435F,40F,

				430F,166F,

				212F, 161F
		)
)