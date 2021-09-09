package net.perfectdreams.loritta.cinnamon.commands.images

import net.perfectdreams.loritta.cinnamon.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class RipTvExecutor(
    emotes: Emotes,
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    emotes,
    client,
    "/api/v1/images/rip-tv",
    "rip_tv.png"
) {
    companion object : CommandExecutorDeclaration(RipTvExecutor::class) {
        override val options = SingleImageOptions
    }
}