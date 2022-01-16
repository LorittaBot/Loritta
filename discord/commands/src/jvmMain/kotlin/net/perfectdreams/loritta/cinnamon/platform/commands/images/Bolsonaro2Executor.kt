package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.SingleImageOptions

class Bolsonaro2Executor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    { client.images.bolsonaro2(it) },
    "bolsonaro_tv2.png"
) {
    companion object : SlashCommandExecutorDeclaration(Bolsonaro2Executor::class) {
        override val options = SingleImageOptions
    }
}