package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.SingleImageOptions

class StudiopolisTvExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    { client.images.studiopolisTv(it) },
    "studiopolis_tv.png"
) {
    companion object : SlashCommandExecutorDeclaration(StudiopolisTvExecutor::class) {
        override val options = SingleImageOptions
    }
}