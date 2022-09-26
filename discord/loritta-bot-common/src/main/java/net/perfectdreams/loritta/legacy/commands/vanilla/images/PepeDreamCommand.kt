package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class PepeDreamCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("pepedream", "sonhopepe", "pepesonho"),
	1,
	"commands.command.pepedream.description",
	"/api/v1/images/pepe-dream",
	"pepe_dream.png",
	slashCommandName = "pepedream"
)