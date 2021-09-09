package net.perfectdreams.loritta.cinnamon.commands.images

import net.perfectdreams.loritta.cinnamon.commands.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.cinnamon.commands.images.base.TwoImagesOptions
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class DrakeExecutor(
    emotes: Emotes,
    client: GabrielaImageServerClient
) : GabrielaImageServerTwoCommandBase(
    emotes,
    client,
    "/api/v1/images/drake",
    "drake.png"
) {
    companion object : CommandExecutorDeclaration(DrakeExecutor::class) {
        override val options = TwoImagesOptions
    }
}