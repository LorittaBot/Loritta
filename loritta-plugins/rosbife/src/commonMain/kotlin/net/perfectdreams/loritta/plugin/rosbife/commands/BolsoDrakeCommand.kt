package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.DrakeBaseCommand

class BolsoDrakeCommand(m: RosbifePlugin) : DrakeBaseCommand(
		m.loritta,
		listOf("bolsodrake"),
		"commands.images.bolsodrake.description",
		"bolsodrake.png",
		1
)