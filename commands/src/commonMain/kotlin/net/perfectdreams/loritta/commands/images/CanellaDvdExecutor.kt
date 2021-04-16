package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration

class CanellaDvdExecutor(
    http: HttpClient
) : GabrielaImageServerSingleCommandBase(
    http,
    "/api/v1/images/canella-dvd",
    "canella_dvd.png"
) {
    companion object : CommandExecutorDeclaration(CanellaDvdExecutor::class) {
        override val options = SingleImageOptions
    }
}