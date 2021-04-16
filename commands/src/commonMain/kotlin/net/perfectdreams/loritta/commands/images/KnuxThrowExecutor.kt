package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration

class KnuxThrowExecutor(
    http: HttpClient
) : GabrielaImageServerSingleCommandBase(
    http,
    "/api/v1/images/knuckles-throw",
    "knux_throw.gif"
) {
    companion object : CommandExecutorDeclaration(KnuxThrowExecutor::class) {
        override val options = SingleImageOptions
    }
}