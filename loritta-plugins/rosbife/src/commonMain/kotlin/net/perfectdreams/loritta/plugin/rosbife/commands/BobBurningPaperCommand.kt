package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

class BobBurningPaperCommand(m: RosbifePlugin) : BasicSkewedImageCommand(
		m.loritta,
		listOf("bobburningpaper", "bobpaperfire", "bobfire", "bobpapelfogo", "bobfogo"),
		"commands.images.bobfire.description",
		"bobfire.png",
		listOf(
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
)