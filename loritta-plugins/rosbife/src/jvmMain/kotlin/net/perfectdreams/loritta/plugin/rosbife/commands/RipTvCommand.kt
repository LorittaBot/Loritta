package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class RipTvCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
	m.loritta,
	listOf("riptv"),
	1,
	"commands.command.riptv.description",
	"/api/v1/images/rip-tv",
	"rip_tv.png",
	slashCommandName = "riptv"
)