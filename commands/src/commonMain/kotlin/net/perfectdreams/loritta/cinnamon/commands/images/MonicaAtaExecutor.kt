package net.perfectdreams.loritta.cinnamon.commands.images

import net.perfectdreams.loritta.cinnamon.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class MonicaAtaExecutor(
    emotes: Emotes,
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    emotes,
    client,
    "/api/v1/images/monica-ata",
    "ata.png"
) {
    companion object : CommandExecutorDeclaration(MonicaAtaExecutor::class) {
        override val options = SingleImageOptions
    }
}