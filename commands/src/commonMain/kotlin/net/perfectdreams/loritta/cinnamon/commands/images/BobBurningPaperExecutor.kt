package net.perfectdreams.loritta.cinnamon.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.cinnamon.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class BobBurningPaperExecutor(
    emotes: Emotes,
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    emotes,
    client,
    "/api/v1/images/bob-burning-paper",
    "bobfire.png"
) {
    companion object : CommandExecutorDeclaration(BobBurningPaperExecutor::class) {
        override val options = SingleImageOptions
    }
}