package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class StudiopolisTvCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("studiopolistv", "studiopolis"),
	1,
	"commands.command.studiopolistv.description",
	"/api/v1/images/studiopolis-tv",
	"studiopolis_tv.png",
	slashCommandName = "sonic studiopolistv"
)