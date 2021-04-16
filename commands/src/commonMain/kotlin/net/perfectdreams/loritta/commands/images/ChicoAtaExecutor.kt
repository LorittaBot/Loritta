package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration

class ChicoAtaExecutor(
    http: HttpClient
) : GabrielaImageServerSingleCommandBase(
    http,
    "/api/v1/images/chico-ata",
    "chico_ata.png"
) {
    companion object : CommandExecutorDeclaration(ChicoAtaExecutor::class) {
        override val options = SingleImageOptions
    }
}