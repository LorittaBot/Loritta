package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.TwoImagesOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class LoriDrakeExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerTwoCommandBase(
    client,
    "/api/v1/images/lori-drake",
    "lori_drake.png"
) {
    companion object : CommandExecutorDeclaration(LoriDrakeExecutor::class) {
        override val options = TwoImagesOptions
    }
}