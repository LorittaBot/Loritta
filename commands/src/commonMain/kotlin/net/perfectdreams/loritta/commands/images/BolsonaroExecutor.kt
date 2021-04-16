package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration

class BolsonaroExecutor(
    http: HttpClient
) : GabrielaImageServerSingleCommandBase(
    http,
    "/api/v1/images/bolsonaro",
    "bolsonaro_tv.png"
) {
    companion object : CommandExecutorDeclaration(BolsonaroExecutor::class) {
        override val options = SingleImageOptions
    }
}