package net.perfectdreams.loritta.legacy.commands.vanilla.images

import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.commands.vanilla.images.base.GabrielaImageServerCommandBase

class AtaCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("ata", "monicaata", "mônicaata"),
	1,
	"commands.command.ata.description",
	"/api/v1/images/monica-ata",
	"ata.png",
	slashCommandName = "ata monica"
)