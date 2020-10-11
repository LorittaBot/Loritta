package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicScaledImageCommand

class StudiopolisTvCommand(m: RosbifePlugin) : BasicScaledImageCommand(
		m.loritta,
		listOf("studiopolis"),
		"commands.images.studiopolistv.description",
		"studiopolis.png",
		190,
		115,
		154,
		61
)