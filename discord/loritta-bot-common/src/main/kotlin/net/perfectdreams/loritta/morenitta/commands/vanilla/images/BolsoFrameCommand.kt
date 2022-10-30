package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class BolsoFrameCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
    m,
    listOf("bolsoframe", "bolsonaroframe", "bolsoquadro", "bolsonaroquadro"),
    1,
    "commands.command.bolsoframe.description",
    "/api/v1/images/bolso-frame",
    "bolsoframe.png",
    slashCommandName = "brmemes bolsonaro frame"
)