package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration

class EdnaldoBandeiraExecutor(
    http: HttpClient
) : GabrielaImageServerSingleCommandBase(
    http,
    "/api/v1/images/ednaldo-bandeira",
    "ednaldo_bandeira.png"
) {
    companion object : CommandExecutorDeclaration(EdnaldoBandeiraExecutor::class) {
        override val options = SingleImageOptions
    }
}