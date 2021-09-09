package net.perfectdreams.loritta.cinnamon.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.cinnamon.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.commands.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.cinnamon.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.commands.images.base.TwoImagesOptions
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class BolsoDrakeExecutor(
    emotes: Emotes,
    client: GabrielaImageServerClient
) : GabrielaImageServerTwoCommandBase(
    emotes,
    client,
    "/api/v1/images/bolso-drake",
    "bolso_drake.png"
) {
    companion object : CommandExecutorDeclaration(BolsoDrakeExecutor::class) {
        override val options = TwoImagesOptions
    }
}