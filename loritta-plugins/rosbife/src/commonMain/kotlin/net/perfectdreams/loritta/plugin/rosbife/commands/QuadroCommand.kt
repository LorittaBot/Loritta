package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

class QuadroCommand(m: RosbifePlugin) : BasicSkewedImageCommand(
		m.loritta,
		listOf("quadro", "frame", "picture", "wolverine"),
		"commands.images.wolverine.description",
		"wolverine.png",
		Corners(
				55F, 165F,
				152F, 159F,
				172F, 283F,
				73F, 293F
		)
)