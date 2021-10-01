package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.TwoImagesOptions
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration

class TrumpExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerTwoCommandBase(
    client,
    { client.images.trump(it) },
    "trump.gif"
) {
    companion object : CommandExecutorDeclaration(TrumpExecutor::class) {
        override val options = TwoImagesOptions
    }
}