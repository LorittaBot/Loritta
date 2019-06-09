package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class BolsonaroCommand : BasicSkewedImageCommand(
		arrayOf("bolsonaro", "bolsonarotv"),
		CommandCategory.IMAGES,
		"commands.images.bolsonaro.description",
		"bolsonaro_tv.png",
		Corners(
				108F,11F,

				383F,8F,

				375F,167F,

				106F, 158F
		)
)