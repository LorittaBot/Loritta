package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class SustoCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
    m,
    listOf("scared", "fright", "susto"),
    1,
    "commands.command.susto.description",
    "/api/v1/images/lori-scared",
    "loritta_susto.png",
    slashCommandName = "scared"
)