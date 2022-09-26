package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class ArtCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("art", "arte"),
	1,
	"commands.command.art.description",
	"/api/v1/images/art",
	"art.png",
	slashCommandName = "art"
)