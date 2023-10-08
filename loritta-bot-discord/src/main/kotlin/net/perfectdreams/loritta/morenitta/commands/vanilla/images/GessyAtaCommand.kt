package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class GessyAtaCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("gessyata"),
	1,
	"commands.command.gessyata.description",
	"/api/v1/images/gessy-ata",
	"gessy_ata.png",
	slashCommandName = "brmemes ata gessy"
)