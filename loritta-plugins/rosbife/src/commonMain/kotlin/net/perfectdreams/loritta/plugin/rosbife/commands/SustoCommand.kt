package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.BasicScaledImageCommand

class SustoCommand(m: RosbifePlugin) : BasicScaledImageCommand(
		m.loritta,
		listOf("scared", "fright", "susto"),
		"commands.images.susto.description",
		"loritta_susto.png",
		84,
		63,
		61,
		138
)