package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.videos

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.base.SingleImageOptions

class AttackOnHeartExecutor(
    loritta: LorittaBot,
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    loritta,
    client,
    { client.videos.attackOnHeart(it) },
    "attack_on_heart.mp4"
)