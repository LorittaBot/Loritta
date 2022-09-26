package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class BolsonaroCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("bolsonaro", "bolsonarotv"),
	1,
	"commands.command.bolsonaro.description",
	"/api/v1/images/bolsonaro",
	"bolsonaro_tv.png",
	slashCommandName = "brmemes bolsonaro tv"
)