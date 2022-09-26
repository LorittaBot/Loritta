package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class PassingPaperCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("passingpaper", "bilhete", "quizkid"),
	1,
	"commands.command.passingpaper.description",
	"/api/v1/images/passing-paper",
	"passing_paper.png",
	slashCommandName = "passingpaper"
)