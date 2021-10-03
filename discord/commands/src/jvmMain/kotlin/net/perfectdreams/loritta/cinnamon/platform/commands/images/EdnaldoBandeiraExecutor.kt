package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.platform.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient

class EdnaldoBandeiraExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    { client.images.ednaldoBandeira(it) },
    "ednaldo_bandeira.png"
) {
    companion object : CommandExecutorDeclaration(EdnaldoBandeiraExecutor::class) {
        override val options = SingleImageOptions
    }
}