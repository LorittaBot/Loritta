package net.perfectdreams.loritta.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.emotes.Emotes

class NichijouYuukoPaperExecutor(
    emotes: Emotes,
    http: HttpClient
) : GabrielaImageServerSingleCommandBase(
    emotes,
    http,
    "/api/v1/images/nichijou-yuuko-paper",
    "nichijou_yuuko_paper.gif"
) {
    companion object : CommandExecutorDeclaration(NichijouYuukoPaperExecutor::class) {
        override val options = SingleImageOptions
    }
}