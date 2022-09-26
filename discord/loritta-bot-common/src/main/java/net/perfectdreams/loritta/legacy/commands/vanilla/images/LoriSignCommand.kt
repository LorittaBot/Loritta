package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class LoriSignCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("lorisign", "lorittasign", "loriplaca", "lorittaplaca"),
	1,
	"commands.command.lorisign.description",
	"/api/v1/images/lori-sign",
	"lori_sign.png",
	slashCommandName = "lorisign"
)