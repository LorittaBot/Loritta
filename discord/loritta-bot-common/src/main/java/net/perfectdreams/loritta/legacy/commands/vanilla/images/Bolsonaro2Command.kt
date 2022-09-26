package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class Bolsonaro2Command(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("bolsonaro2", "bolsonarotv2"),
	1,
	"commands.command.bolsonaro.description",
	"/api/v1/images/bolsonaro2",
	"bolsonaro_tv2.png",
	slashCommandName = "brmemes bolsonaro tv2"
)