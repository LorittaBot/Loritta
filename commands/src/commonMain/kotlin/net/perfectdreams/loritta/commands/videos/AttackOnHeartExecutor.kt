package net.perfectdreams.loritta.commands.videos

import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.utils.gabrielaimageserver.GabrielaImageServerClient

class AttackOnHeartExecutor(
    emotes: Emotes,
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    emotes,
    client,
    "/api/v1/videos/attack-on-heart",
    "attack_on_heart.mp4"
) {
    companion object : CommandExecutorDeclaration(AttackOnHeartExecutor::class) {
        override val options = SingleImageOptions
    }
}