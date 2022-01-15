package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.TwoImagesOptions

class DrakeExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerTwoCommandBase(
    client,
    { client.images.drake(it) },
    "drake.png"
) {
    companion object : SlashCommandExecutorDeclaration(DrakeExecutor::class) {
        override val options = TwoImagesOptions
    }
}