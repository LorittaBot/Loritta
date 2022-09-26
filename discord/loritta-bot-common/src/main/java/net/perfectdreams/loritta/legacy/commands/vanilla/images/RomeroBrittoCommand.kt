package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class RomeroBrittoCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("romerobritto", "pintura", "painting"),
	1,
	"commands.command.romerobritto.description",
	"/api/v1/images/romero-britto",
	"romero_britto.png",
	slashCommandName = "brmemes romerobritto"
)