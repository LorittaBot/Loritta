package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.DrakeBaseCommand

class DrakeCommand(m: RosbifePlugin) : DrakeBaseCommand(
		m.loritta,
		listOf("drake"),
		"commands.images.drake.description",
		"drake.png",
		1
)