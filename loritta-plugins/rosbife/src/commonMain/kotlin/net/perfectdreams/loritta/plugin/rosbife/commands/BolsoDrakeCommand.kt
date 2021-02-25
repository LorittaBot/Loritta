package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class BolsoDrakeCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("bolsodrake"),
		2,
		"commands.command.bolsodrake.description",
		"/api/v1/images/bolso-drake",
		"bolsodrake.png",
)