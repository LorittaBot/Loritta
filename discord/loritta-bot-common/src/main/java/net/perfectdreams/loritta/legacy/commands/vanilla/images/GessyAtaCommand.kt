package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class GessyAtaCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("gessyata"),
	1,
	"commands.command.gessyata.description",
	"/api/v1/images/gessy-ata",
	"gessy_ata.png",
	slashCommandName = "brmemes ata gessy"
)