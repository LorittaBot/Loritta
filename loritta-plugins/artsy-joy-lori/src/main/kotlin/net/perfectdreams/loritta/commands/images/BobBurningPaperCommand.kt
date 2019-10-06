package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class BobBurningPaperCommand : BasicSkewedImageCommand(
		arrayOf("bobburningpaper", "bobpaperfire", "bobfire", "bobpapelfogo", "bobfogo"),
		CommandCategory.IMAGES,
		"commands.images.bobfire.description",
		"bobfire.png",
		Corners(
				21f, 373f,

				14f, 353f,

				48f, 334f,

				82f, 354f
		),
		Corners(
				24f, 32f,

				138f, 33f,

				137f, 177f,

				20f, 175f
		)
)