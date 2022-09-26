package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class RipTvCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("riptv"),
	1,
	"commands.command.riptv.description",
	"/api/v1/images/rip-tv",
	"rip_tv.png",
	slashCommandName = "riptv"
)