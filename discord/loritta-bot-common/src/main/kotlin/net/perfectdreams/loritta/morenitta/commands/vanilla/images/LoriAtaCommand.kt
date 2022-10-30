package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class LoriAtaCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
    m,
    listOf("loriata"),
    1,
    "commands.command.loriata.description",
    "/api/v1/images/lori-ata",
    "lori_ata.png",
    slashCommandName = "brmemes ata lori"
)