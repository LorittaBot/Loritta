package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class PepeDreamCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("pepedream", "sonhopepe", "pepesonho"),
	1,
	"commands.command.pepedream.description",
	"/api/v1/images/pepe-dream",
	"pepe_dream.png",
	slashCommandName = "pepedream"
)