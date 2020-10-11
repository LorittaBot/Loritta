package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

class GessyAtaCommand(m: RosbifePlugin) : BasicSkewedImageCommand(
		m.loritta,
		listOf("gessyata"),
		"commands.images.gessyata.description",
		"gessyata.png",
		Corners(
				130F, 35F,
				410F, 92F,
				387F, 277F,
				111F, 210F
		)
)