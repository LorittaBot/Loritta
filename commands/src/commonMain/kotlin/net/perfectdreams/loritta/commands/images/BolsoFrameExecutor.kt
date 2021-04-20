package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.emotes.Emotes

class BolsoFrameExecutor(
    emotes: Emotes,
    http: HttpClient
) : GabrielaImageServerSingleCommandBase(
    emotes,
    http,
    "/api/v1/images/bolso-frame",
    "bolsoframe.png"
) {
    companion object : CommandExecutorDeclaration(BolsoFrameExecutor::class) {
        override val options = SingleImageOptions
    }
}