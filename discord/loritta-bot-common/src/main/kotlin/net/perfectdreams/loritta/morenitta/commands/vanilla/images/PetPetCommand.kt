package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class PetPetCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
    m,
    listOf("petpet"),
    1,
    "commands.command.petpet.description",
    "/api/v1/images/pet-pet",
    "petpet.gif",
    slashCommandName = "petpet"
)
