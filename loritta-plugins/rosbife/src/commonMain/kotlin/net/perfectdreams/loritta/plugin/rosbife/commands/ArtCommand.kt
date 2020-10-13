package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

class ArtCommand(m: RosbifePlugin) : BasicSkewedImageCommand(
		m.loritta,
		listOf("art", "arte"),
		"commands.images.art.description",
		"art.png",
		Corners(
				75f, 215f,
				172f, 242f,
				106f, 399f,
				13f, 369f
		)
)