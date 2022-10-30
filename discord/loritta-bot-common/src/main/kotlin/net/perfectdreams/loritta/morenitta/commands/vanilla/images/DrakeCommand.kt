package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class DrakeCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
    m,
    listOf("drake"),
    2,
    "commands.command.drake.description",
    "/api/v1/images/drake",
    "drake.png",
    slashCommandName = "drake drake"
)
