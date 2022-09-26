package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class RipTvCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("riptv"),
	1,
	"commands.command.riptv.description",
	"/api/v1/images/rip-tv",
	"rip_tv.png",
	slashCommandName = "riptv"
)