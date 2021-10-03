package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class DrakeCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
	m.loritta,
	listOf("drake"),
	2,
	"commands.command.drake.description",
	"/api/v1/images/drake",
	"drake.png",
	slashCommandName = "drake drake"
)
