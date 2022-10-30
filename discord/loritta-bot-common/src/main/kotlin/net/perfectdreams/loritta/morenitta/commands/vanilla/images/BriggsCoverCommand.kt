package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class BriggsCoverCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
    m,
    listOf("briggscover", "coverbriggs", "capabriggs", "briggscapa"),
    1,
    "commands.command.briggscover.description",
    "/api/v1/images/briggs-cover",
    "briggs_capa.png",
    slashCommandName = "brmemes briggscover"
)