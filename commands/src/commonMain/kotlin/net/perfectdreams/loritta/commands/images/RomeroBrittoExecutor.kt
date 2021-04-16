package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration

class RomeroBrittoExecutor(
    http: HttpClient
) : GabrielaImageServerSingleCommandBase(
    http,
    "/api/v1/images/romero-britto",
    "romero_britto.png"
) {
    companion object : CommandExecutorDeclaration(RomeroBrittoExecutor::class) {
        override val options = SingleImageOptions
    }
}