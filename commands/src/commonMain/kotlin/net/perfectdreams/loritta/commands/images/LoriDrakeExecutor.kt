package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.commands.images.base.TwoImagesOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration

class LoriDrakeExecutor(
    http: HttpClient
) : GabrielaImageServerTwoCommandBase(
    http,
    "/api/v1/images/lori-drake",
    "lori_drake.png"
) {
    companion object : CommandExecutorDeclaration(LoriDrakeExecutor::class) {
        override val options = TwoImagesOptions
    }
}