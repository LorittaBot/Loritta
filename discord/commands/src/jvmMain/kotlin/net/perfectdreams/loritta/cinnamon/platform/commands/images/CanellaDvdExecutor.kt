package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.SingleImageOptions

class CanellaDvdExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    { client.images.canellaDvd(it) },
    "canella_dvd.png"
) {
    companion object : SlashCommandExecutorDeclaration(CanellaDvdExecutor::class) {
        override val options = SingleImageOptions
    }
}