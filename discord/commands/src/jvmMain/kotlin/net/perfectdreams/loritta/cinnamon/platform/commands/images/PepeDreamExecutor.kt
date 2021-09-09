package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class PepeDreamExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    "/api/v1/images/pepe-dream",
    "pepe_dream.png"
) {
    companion object : CommandExecutorDeclaration(PepeDreamExecutor::class) {
        override val options = SingleImageOptions
    }
}