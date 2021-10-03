package net.perfectdreams.loritta.plugin.rosbife.commands

import net.perfectdreams.loritta.plugin.rosbife.RosbifePlugin
import net.perfectdreams.loritta.plugin.rosbife.commands.base.GabrielaImageServerCommandBase

class PassingPaperCommand(m: RosbifePlugin) : GabrielaImageServerCommandBase(
	m.loritta,
	listOf("passingpaper", "bilhete", "quizkid"),
	1,
	"commands.command.passingpaper.description",
	"/api/v1/images/passing-paper",
	"passing_paper.png",
	slashCommandName = "passingpaper"
)