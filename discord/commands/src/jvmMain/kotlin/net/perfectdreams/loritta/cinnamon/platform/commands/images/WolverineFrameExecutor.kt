package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient

class WolverineFrameExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    { client.images.wolverineFrame(it) },
    "wolverine_frame.png"
) {
    companion object : CommandExecutorDeclaration(WolverineFrameExecutor::class) {
        override val options = SingleImageOptions
    }
}