package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

class LoriSignCommand(m: RosbifePlugin) : BasicSkewedImageCommand(
		m.loritta,
		listOf("lorisign", "lorittasign", "loriplaca", "lorittaplaca"),
		"commands.images.lorisign.description",
		"loritta_placa.png",
		Corners(
				20f, 202f,
				155f, 226f,
				139f, 299f,
				3f, 275f
		)
)