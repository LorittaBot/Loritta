package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.base.SingleImageOptions

class CarlyAaahExecutor(
    loritta: LorittaBot,
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    loritta,
    client,
    { client.videos.carlyAaah(it) },
    "carly_aaah.mp4"
)