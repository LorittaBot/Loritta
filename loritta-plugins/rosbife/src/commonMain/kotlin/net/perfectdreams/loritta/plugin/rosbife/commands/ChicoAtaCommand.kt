package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

class ChicoAtaCommand(m: RosbifePlugin) : BasicSkewedImageCommand(
		m.loritta,
		listOf("chicoata"),
		"commands.images.chicoata.description",
		"chicoata.png",
		Corners(
				300F, 0F,
				768F, 0F,
				768F, 480F,
				300F, 383F
		)
)