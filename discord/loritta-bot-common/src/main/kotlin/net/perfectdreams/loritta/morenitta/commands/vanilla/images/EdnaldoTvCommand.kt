package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class EdnaldoTvCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
    m,
    listOf("ednaldotv"),
    1,
    "commands.command.ednaldotv.description",
    "/api/v1/images/ednaldo-tv",
    "ednaldo_tv.png",
    slashCommandName = "brmemes ednaldo tv"
)
