package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicScaledImageCommand

class PepeDreamCommand(m: RosbifePlugin) : BasicScaledImageCommand(
		m.loritta,
		listOf("pepedream", "sonhopepe", "pepesonho"),
		"commands.images.pepedream.description",
		"pepedream.png",
		100,
		100,
		81,
		2
)