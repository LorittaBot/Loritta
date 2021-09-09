package net.perfectdreams.loritta.cinnamon.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.cinnamon.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class Bolsonaro2Executor(
    emotes: Emotes,
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    emotes,
    client,
    "/api/v1/images/bolsonaro2",
    "bolsonaro_tv2.png"
) {
    companion object : CommandExecutorDeclaration(Bolsonaro2Executor::class) {
        override val options = SingleImageOptions
    }
}