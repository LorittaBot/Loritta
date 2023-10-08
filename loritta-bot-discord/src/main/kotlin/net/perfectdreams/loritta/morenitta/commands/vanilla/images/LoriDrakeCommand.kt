package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class LoriDrakeCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
	m,
	listOf("loridrake"),
	2,
	"commands.command.loridrake.description",
	"/api/v1/images/lori-drake",
	"lori_drake.png",
	slashCommandName = "drake lori"
)