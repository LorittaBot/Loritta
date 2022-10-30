package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class EdnaldoBandeiraCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
    m,
    listOf("ednaldobandeira"),
    1,
    "commands.command.ednaldobandeira.description",
    "/api/v1/images/ednaldo-bandeira",
    "ednaldo_bandeira.png",
    slashCommandName = "brmemes ednaldo bandeira"
)
