package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class ArtCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
    m,
    listOf("art", "arte"),
    1,
    "commands.command.art.description",
    "/api/v1/images/art",
    "art.png",
    slashCommandName = "art"
)