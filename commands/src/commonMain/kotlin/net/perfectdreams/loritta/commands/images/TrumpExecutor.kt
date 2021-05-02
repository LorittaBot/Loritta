package net.perfectdreams.loritta.commands.images

import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.commands.images.base.TwoImagesOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.utils.gabrielaimageserver.GabrielaImageServerClient

class TrumpExecutor(
    emotes: Emotes,
    client: GabrielaImageServerClient
) : GabrielaImageServerTwoCommandBase(
    emotes,
    client,
    "/api/v1/images/trump",
    "trump.gif"
) {
    companion object : CommandExecutorDeclaration(TrumpExecutor::class) {
        override val options = TwoImagesOptions
    }
}