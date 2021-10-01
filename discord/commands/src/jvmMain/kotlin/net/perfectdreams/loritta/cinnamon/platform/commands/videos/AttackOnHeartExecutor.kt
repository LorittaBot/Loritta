package net.perfectdreams.loritta.cinnamon.platform.commands.videos

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.SingleImageOptions

class AttackOnHeartExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    { client.videos.attackOnHeart(it) },
    "attack_on_heart.mp4"
) {
    companion object : CommandExecutorDeclaration(AttackOnHeartExecutor::class) {
        override val options = SingleImageOptions
    }
}