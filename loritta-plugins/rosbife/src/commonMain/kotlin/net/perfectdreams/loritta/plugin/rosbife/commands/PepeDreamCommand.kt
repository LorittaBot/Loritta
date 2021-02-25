package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class PepeDreamCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
		m.loritta,
		listOf("pepedream", "sonhopepe", "pepesonho"),
		1,
		"commands.command.pepedream.description",
		"/api/v1/images/pepe-dream",
		"pepe_dream.png",
)