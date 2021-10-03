package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class LoriDrakeCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
	m.loritta,
	listOf("loridrake"),
	2,
	"commands.command.loridrake.description",
	"/api/v1/images/lori-drake",
	"lori_drake.png",
	slashCommandName = "drake lori"
)