package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class EdnaldoBandeiraCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("ednaldobandeira"),
	1,
	"commands.command.ednaldobandeira.description",
	"/api/v1/images/ednaldo-bandeira",
	"ednaldo_bandeira.png",
	slashCommandName = "brmemes ednaldo bandeira"
)
