package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

class LoriAtaCommand(m: RosbifePlugin) : BasicSkewedImageCommand(
		m.loritta,
		listOf("loriata"),
		"commands.images.loriata.description",
		"loriata.png",
		Corners(
				273F, 0F,
				768F, 0F,
				768F, 454F,
				245F, 354F
		)
)