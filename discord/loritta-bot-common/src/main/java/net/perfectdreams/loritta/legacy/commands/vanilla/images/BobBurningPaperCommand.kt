package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class BobBurningPaperCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("bobburningpaper", "bobpaperfire", "bobfire", "bobpapelfogo", "bobfogo"),
	1,
	"commands.command.bobfire.description",
	"/api/v1/images/bob-burning-paper",
	"bobfire.png",
	slashCommandName = "bobburningpaper"
)