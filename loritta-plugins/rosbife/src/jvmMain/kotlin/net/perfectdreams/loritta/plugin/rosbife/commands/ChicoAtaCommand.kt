package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class ChicoAtaCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
	m.loritta,
	listOf("chicoata"),
	1,
	"commands.command.chicoata.description",
	"/api/v1/images/chico-ata",
	"chico_ata.png",
	slashCommandName = "brmemes ata chico"
)