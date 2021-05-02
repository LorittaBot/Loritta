package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.commands.images.base.TwoImagesOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.utils.gabrielaimageserver.GabrielaImageServerClient

class LoriDrakeExecutor(
    emotes: Emotes,
    client: GabrielaImageServerClient
) : GabrielaImageServerTwoCommandBase(
    emotes,
    client,
    "/api/v1/images/lori-drake",
    "lori_drake.png"
) {
    companion object : CommandExecutorDeclaration(LoriDrakeExecutor::class) {
        override val options = TwoImagesOptions
    }
}