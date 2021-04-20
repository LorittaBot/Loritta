package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration

class ToBeContinuedExecutor(
    http: HttpClient
) : GabrielaImageServerSingleCommandBase(
    http,
    "/api/v1/images/to-be-continued",
    "to_be_continued.png"
) {
    companion object : CommandExecutorDeclaration(ToBeContinuedExecutor::class) {
        override val options = SingleImageOptions
    }
}