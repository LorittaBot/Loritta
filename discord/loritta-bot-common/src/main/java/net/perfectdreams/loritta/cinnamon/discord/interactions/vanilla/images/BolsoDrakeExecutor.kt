package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.base.TwoImagesOptions

class BolsoDrakeExecutor(
    loritta: LorittaCinnamon,
    client: GabrielaImageServerClient
) : GabrielaImageServerTwoCommandBase(
    loritta,
    client,
    { client.images.bolsoDrake(it) },
    "bolso_drake.png"
)