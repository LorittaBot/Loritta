package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.TwoImagesOptions

class LoriDrakeExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerTwoCommandBase(
    client,
    { client.images.loriDrake(it) },
    "lori_drake.png"
) {
    companion object : CommandExecutorDeclaration(LoriDrakeExecutor::class) {
        override val options = TwoImagesOptions
    }
}