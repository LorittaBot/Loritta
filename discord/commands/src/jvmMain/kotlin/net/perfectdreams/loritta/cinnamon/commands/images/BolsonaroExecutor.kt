package net.perfectdreams.loritta.cinnamon.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.cinnamon.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class BolsonaroExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    "/api/v1/images/bolsonaro",
    "bolsonaro_tv.png"
) {
    companion object : CommandExecutorDeclaration(BolsonaroExecutor::class) {
        override val options = SingleImageOptions
    }
}