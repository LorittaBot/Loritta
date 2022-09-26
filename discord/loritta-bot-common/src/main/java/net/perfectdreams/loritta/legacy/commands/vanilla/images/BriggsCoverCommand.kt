package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class BriggsCoverCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("briggscover", "coverbriggs", "capabriggs", "briggscapa"),
	1,
	"commands.command.briggscover.description",
	"/api/v1/images/briggs-cover",
	"briggs_capa.png",
	slashCommandName = "brmemes briggscover"
)