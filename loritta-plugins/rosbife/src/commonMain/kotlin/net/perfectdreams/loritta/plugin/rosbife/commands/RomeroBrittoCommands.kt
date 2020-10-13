package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicSkewedImageCommand

class RomeroBrittoCommands(m: RosbifePlugin) : BasicSkewedImageCommand(
		m.loritta,
		listOf("romerobritto", "pintura", "painting"),
		"commands.images.romerobritto.description",
		"romero_britto.png",
		Corners(
				16F,19F,
				201F,34F,
				208F,218F,
				52F, 294F
		)
)