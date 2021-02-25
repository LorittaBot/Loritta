package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class RomeroBrittoCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("romerobritto", "pintura", "painting"),
		1,
		"commands.command.romerobritto.description",
		"/api/v1/images/romero-britto",
		"romero_britto.png",
)