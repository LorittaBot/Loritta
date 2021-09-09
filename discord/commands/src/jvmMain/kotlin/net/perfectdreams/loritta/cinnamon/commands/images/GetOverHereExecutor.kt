package net.perfectdreams.loritta.cinnamon.commands.images

import net.perfectdreams.loritta.cinnamon.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class GetOverHereExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    "/api/v1/images/get-over-here",
    "get_over_here.gif"
) {
    companion object : CommandExecutorDeclaration(GetOverHereExecutor::class) {
        override val options = SingleImageOptions
    }
}