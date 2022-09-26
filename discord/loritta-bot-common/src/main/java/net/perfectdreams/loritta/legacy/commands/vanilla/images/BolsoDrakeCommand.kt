package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class BolsoDrakeCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("bolsodrake"),
	2,
	"commands.command.bolsodrake.description",
	"/api/v1/images/bolso-drake",
	"bolsodrake.png",
	slashCommandName = "drake bolsonaro"
)