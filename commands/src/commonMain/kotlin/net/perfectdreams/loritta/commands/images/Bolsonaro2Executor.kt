package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.emotes.Emotes

class Bolsonaro2Executor(
    emotes: Emotes,
    http: HttpClient
) : GabrielaImageServerSingleCommandBase(
    emotes,
    http,
    "/api/v1/images/bolsonaro2",
    "bolsonaro_tv2.png"
) {
    companion object : CommandExecutorDeclaration(Bolsonaro2Executor::class) {
        override val options = SingleImageOptions
    }
}