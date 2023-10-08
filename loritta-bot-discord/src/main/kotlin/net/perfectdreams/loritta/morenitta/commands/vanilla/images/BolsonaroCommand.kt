package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class BolsonaroCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("bolsonaro", "bolsonarotv"),
	1,
	"commands.command.bolsonaro.description",
	"/api/v1/images/bolsonaro",
	"bolsonaro_tv.png",
	slashCommandName = "brmemes bolsonaro tv"
)