package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class ChicoAtaCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("chicoata"),
	1,
	"commands.command.chicoata.description",
	"/api/v1/images/chico-ata",
	"chico_ata.png",
	slashCommandName = "brmemes ata chico"
)