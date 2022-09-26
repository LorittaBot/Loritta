package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class SustoCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("scared", "fright", "susto"),
	1,
	"commands.command.susto.description",
	"/api/v1/images/lori-scared",
	"loritta_susto.png",
	slashCommandName = "scared"
)