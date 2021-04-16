package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration

class BriggsCoverExecutor(
    http: HttpClient
) : GabrielaImageServerSingleCommandBase(
    http,
    "/api/v1/images/briggs-cover",
    "briggs_capa.png"
) {
    companion object : CommandExecutorDeclaration(BriggsCoverExecutor::class) {
        override val options = SingleImageOptions
    }
}