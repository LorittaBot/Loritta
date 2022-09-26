package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class BolsoFrameCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("bolsoframe", "bolsonaroframe", "bolsoquadro", "bolsonaroquadro"),
	1,
	"commands.command.bolsoframe.description",
	"/api/v1/images/bolso-frame",
	"bolsoframe.png",
	slashCommandName = "brmemes bolsonaro frame"
)