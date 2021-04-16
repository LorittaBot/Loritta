package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration

class CarlyAaahExecutor(
    http: HttpClient
) : GabrielaImageServerSingleCommandBase(
    http,
    "/api/v1/videos/carly-aaah",
    "carly_aaah.mp4"
) {
    companion object : CommandExecutorDeclaration(CarlyAaahExecutor::class) {
        override val options = SingleImageOptions
    }
}