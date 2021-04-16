package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.commands.images.base.TwoImagesOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration

class BolsoDrakeExecutor(
    http: HttpClient
) : GabrielaImageServerTwoCommandBase(
    http,
    "/api/v1/images/bolso-drake",
    "bolso_drake.png"
) {
    companion object : CommandExecutorDeclaration(BolsoDrakeExecutor::class) {
        override val options = TwoImagesOptions
    }
}