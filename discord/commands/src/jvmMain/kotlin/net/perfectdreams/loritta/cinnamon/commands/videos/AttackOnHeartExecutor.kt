package net.perfectdreams.loritta.cinnamon.commands.videos

import net.perfectdreams.loritta.cinnamon.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class AttackOnHeartExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    "/api/v1/videos/attack-on-heart",
    "attack_on_heart.mp4"
) {
    companion object : CommandExecutorDeclaration(AttackOnHeartExecutor::class) {
        override val options = SingleImageOptions
    }
}