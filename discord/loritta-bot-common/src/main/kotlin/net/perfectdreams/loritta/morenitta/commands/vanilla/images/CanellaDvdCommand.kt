package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class CanellaDvdCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
    m,
    listOf("canelladvd", "matheuscanelladvd", "canellacover", "matheuscanelladvd"),
    1,
    "commands.command.canelladvd.description",
    "/api/v1/images/canella-dvd",
    "canella_dvd.png",
    slashCommandName = "brmemes canelladvd"
)