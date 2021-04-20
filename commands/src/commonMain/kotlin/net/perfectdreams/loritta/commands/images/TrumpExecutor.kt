package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.commands.images.base.TwoImagesOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.emotes.Emotes

class TrumpExecutor(
    emotes: Emotes,
    http: HttpClient
) : GabrielaImageServerTwoCommandBase(
    emotes,
    http,
    "/api/v1/images/trump",
    "trump.gif"
) {
    companion object : CommandExecutorDeclaration(TrumpExecutor::class) {
        override val options = TwoImagesOptions
    }
}