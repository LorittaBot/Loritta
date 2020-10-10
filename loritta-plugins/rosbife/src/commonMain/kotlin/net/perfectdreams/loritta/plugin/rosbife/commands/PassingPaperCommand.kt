package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

class PassingPaperCommand(m: RosbifePlugin) : BasicSkewedImageCommand(
		m.loritta,
		listOf("passingpaper", "bilhete", "quizkid"),
		"commands.images.passingpaper.description",
		"passingpaper.png",
		Corners(
				220f, 210f,
				318f, 245f,
				266f, 335f,
				174f, 283f
		)
)