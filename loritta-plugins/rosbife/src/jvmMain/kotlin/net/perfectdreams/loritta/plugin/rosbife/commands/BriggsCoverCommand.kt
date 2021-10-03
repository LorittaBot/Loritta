package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class BriggsCoverCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
	m.loritta,
	listOf("briggscover", "coverbriggs", "capabriggs", "briggscapa"),
	1,
	"commands.command.briggscover.description",
	"/api/v1/images/briggs-cover",
	"briggs_capa.png",
	slashCommandName = "brmemes briggscover"
)