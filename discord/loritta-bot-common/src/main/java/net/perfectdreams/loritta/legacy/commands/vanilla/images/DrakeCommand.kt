package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class DrakeCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("drake"),
	2,
	"commands.command.drake.description",
	"/api/v1/images/drake",
	"drake.png",
	slashCommandName = "drake drake"
)
