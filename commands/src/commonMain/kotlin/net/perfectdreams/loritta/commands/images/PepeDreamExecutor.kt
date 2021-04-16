package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration

class PepeDreamExecutor(
    http: HttpClient
) : GabrielaImageServerSingleCommandBase(
    http,
    "/api/v1/images/pepe-dream",
    "pepe_dream.png"
) {
    companion object : CommandExecutorDeclaration(PepeDreamExecutor::class) {
        override val options = SingleImageOptions
    }
}