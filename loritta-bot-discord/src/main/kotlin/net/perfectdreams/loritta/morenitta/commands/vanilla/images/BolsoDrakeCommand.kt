package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class BolsoDrakeCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("bolsodrake"),
	2,
	"commands.command.bolsodrake.description",
	"/api/v1/images/bolso-drake",
	"bolsodrake.png",
	slashCommandName = "drake bolsonaro"
)