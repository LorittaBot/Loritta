package net.perfectdreams.loritta.cinnamon.commands.images

import net.perfectdreams.loritta.cinnamon.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.cinnamon.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.cinnamon.discord.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient

class LoriAtaExecutor(
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    client,
    "/api/v1/images/lori-ata",
    "lori_ata.png"
) {
    companion object : CommandExecutorDeclaration(LoriAtaExecutor::class) {
        override val options = SingleImageOptions
    }
}