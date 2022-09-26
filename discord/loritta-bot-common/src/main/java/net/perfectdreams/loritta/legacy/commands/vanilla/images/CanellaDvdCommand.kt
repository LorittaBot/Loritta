package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class CanellaDvdCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("canelladvd", "matheuscanelladvd", "canellacover", "matheuscanelladvd"),
	1,
	"commands.command.canelladvd.description",
	"/api/v1/images/canella-dvd",
	"canella_dvd.png",
	slashCommandName = "brmemes canelladvd"
)