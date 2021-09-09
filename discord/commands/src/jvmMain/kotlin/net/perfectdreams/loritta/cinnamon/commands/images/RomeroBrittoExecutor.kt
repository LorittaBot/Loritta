package net.perfectdreams.loritta.cinnamon.commands.images

import net.perfectdreams.loritta.cinnamon.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class RomeroBrittoExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    "/api/v1/images/romero-britto",
    "romero_britto.png"
) {
    companion object : CommandExecutorDeclaration(RomeroBrittoExecutor::class) {
        override val options = SingleImageOptions
    }
}