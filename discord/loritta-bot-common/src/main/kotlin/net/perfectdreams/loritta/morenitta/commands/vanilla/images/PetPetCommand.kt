package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class PetPetCommand(m: LorittaDiscord) : GabrielaImageServerCommandBase(
	m,
	listOf("petpet"),
	1,
	"commands.command.petpet.description",
	"/api/v1/images/pet-pet",
	"petpet.gif",
	slashCommandName = "petpet"
)
