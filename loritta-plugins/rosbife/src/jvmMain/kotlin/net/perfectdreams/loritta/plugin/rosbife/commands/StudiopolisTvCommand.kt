package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class StudiopolisTvCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
	m.loritta,
	listOf("studiopolistv", "studiopolis"),
	1,
	"commands.command.studiopolistv.description",
	"/api/v1/images/studiopolis-tv",
	"studiopolis_tv.png",
	slashCommandName = "sonic studiopolistv"
)