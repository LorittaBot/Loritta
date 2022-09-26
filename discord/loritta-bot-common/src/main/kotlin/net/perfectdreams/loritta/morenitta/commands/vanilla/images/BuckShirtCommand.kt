package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class BuckShirtCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("buckshirt", "buckcamisa"),
	1,
	"commands.command.buckshirt.description",
	"/api/v1/images/buck-shirt",
	"buck_shirt.png",
	slashCommandName = "buckshirt"
)