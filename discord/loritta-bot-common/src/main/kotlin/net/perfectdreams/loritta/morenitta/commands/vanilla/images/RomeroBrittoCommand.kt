package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class RomeroBrittoCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("romerobritto", "pintura", "painting"),
	1,
	"commands.command.romerobritto.description",
	"/api/v1/images/romero-britto",
	"romero_britto.png",
	slashCommandName = "brmemes romerobritto"
)