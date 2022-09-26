package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class StudiopolisTvCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("studiopolistv", "studiopolis"),
	1,
	"commands.command.studiopolistv.description",
	"/api/v1/images/studiopolis-tv",
	"studiopolis_tv.png",
	slashCommandName = "sonic studiopolistv"
)