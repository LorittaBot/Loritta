package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class RomeroBrittoCommands(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("romerobritto", "pintura", "painting"),
		1,
		"commands.images.romerobritto.description",
		"/api/v1/images/romero-britto",
		"romero_britto.png",
)