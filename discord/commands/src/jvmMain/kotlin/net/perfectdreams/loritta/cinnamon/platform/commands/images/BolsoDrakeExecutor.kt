package net.perfectdreams.loritta.cinnamon.platform.commands.images

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.platform.commands.SlashCommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.GabrielaImageServerTwoCommandBase
import net.perfectdreams.loritta.cinnamon.platform.commands.images.base.TwoImagesOptions

class BolsoDrakeExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerTwoCommandBase(
    client,
    { client.images.bolsoDrake(it) },
    "bolso_drake.png"
) {
    companion object : SlashCommandExecutorDeclaration(BolsoDrakeExecutor::class) {
        override val options = TwoImagesOptions
    }
}