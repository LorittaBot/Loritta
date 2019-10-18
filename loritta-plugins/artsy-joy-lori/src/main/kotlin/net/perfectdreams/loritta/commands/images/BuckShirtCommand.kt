package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.api.commands.CommandCategory

class BuckShirtCommand : BasicSkewedImageCommand(
		arrayOf("buckshirt", "buckcamisa"),
		CommandCategory.IMAGES,
		"commands.images.buckshirt.description",
		"buckshirt.png",
		Corners(
				47f, 90f,

				83f, 91f,

				86f, 133f,

				52f, 133f
		),
		Corners(
				59f, 209f,

				79f, 210f,

				80f, 233f,

				60f, 234f
		),
		Corners(
				226f, 105f,

				335f, 113f,

				306f, 236f,

				193f, 218f
		)
)