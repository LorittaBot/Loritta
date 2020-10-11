package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.DrakeBaseCommand

class LoriDrakeCommand(m: RosbifePlugin) : DrakeBaseCommand(
		m.loritta,
		listOf("loridrake"),
		"commands.images.loridrake.description",
		"lori_drake.png",
		2
)