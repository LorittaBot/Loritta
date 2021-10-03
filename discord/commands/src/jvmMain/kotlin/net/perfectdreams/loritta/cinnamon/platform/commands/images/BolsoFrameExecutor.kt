package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient

class BolsoFrameExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    { client.images.bolsoFrame(it) },
    "bolsoframe.png"
) {
    companion object : CommandExecutorDeclaration(BolsoFrameExecutor::class) {
        override val options = SingleImageOptions
    }
}