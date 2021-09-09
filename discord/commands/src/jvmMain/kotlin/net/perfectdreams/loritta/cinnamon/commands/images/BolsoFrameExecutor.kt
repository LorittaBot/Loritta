package net.perfectdreams.loritta.cinnamon.commands.images

import io.ktor.client.*
import net.perfectdreams.loritta.cinnamon.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class BolsoFrameExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    "/api/v1/images/bolso-frame",
    "bolsoframe.png"
) {
    companion object : CommandExecutorDeclaration(BolsoFrameExecutor::class) {
        override val options = SingleImageOptions
    }
}