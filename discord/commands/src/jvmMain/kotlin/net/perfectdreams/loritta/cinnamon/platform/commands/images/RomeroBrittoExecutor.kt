package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient

class RomeroBrittoExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    { client.images.romeroBritto(it) },
    "romero_britto.png"
) {
    companion object : CommandExecutorDeclaration(RomeroBrittoExecutor::class) {
        override val options = SingleImageOptions
    }
}