package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class AtaCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
	m.loritta,
	listOf("ata", "monicaata", "mônicaata"),
	1,
	"commands.command.ata.description",
	"/api/v1/images/monica-ata",
	"ata.png",
	slashCommandName = "ata monica"
)