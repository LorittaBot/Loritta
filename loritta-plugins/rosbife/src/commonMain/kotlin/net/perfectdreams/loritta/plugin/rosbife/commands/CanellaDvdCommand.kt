package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class CanellaDvdCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("canelladvd", "matheuscanelladvd", "canellacover", "matheuscanelladvd"),
		1,
		"commands.command.canelladvd.description",
		"/api/v1/images/canella-dvd",
		"canella_dvd.png",
)