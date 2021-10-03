package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class BolsoFrameCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
	m.loritta,
	listOf("bolsoframe", "bolsonaroframe", "bolsoquadro", "bolsonaroquadro"),
	1,
	"commands.command.bolsoframe.description",
	"/api/v1/images/bolso-frame",
	"bolsoframe.png",
	slashCommandName = "brmemes bolsonaro frame"
)