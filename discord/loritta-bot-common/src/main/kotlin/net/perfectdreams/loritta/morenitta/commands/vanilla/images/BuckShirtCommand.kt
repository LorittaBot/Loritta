package net.perfectdreams.loritta.morenitta.commands.vanilla.images

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.images.base.GabrielaImageServerCommandBase

class BuckShirtCommand(m: LorittaBot) : GabrielaImageServerCommandBase(
    m,
    listOf("buckshirt", "buckcamisa"),
    1,
    "commands.command.buckshirt.description",
    "/api/v1/images/buck-shirt",
    "buck_shirt.png",
    slashCommandName = "buckshirt"
)