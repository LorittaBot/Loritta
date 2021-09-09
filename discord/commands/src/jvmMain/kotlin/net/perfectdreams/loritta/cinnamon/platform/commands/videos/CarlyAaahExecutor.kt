package net.perfectdreams.loritta.cinnamon.platform.commands.videos

import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class CarlyAaahExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    "/api/v1/videos/carly-aaah",
    "carly_aaah.mp4"
) {
    companion object : CommandExecutorDeclaration(CarlyAaahExecutor::class) {
        override val options = SingleImageOptions
    }
}