package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.morenitta.LorittaBot

class DrakeExecutor(
    loritta: LorittaBot,
    client: GabrielaImageServerClient
) : GabrielaImageServerTwoCommandBase(
    loritta,
    client,
    { client.images.drake(it) },
    "drake.png"
)