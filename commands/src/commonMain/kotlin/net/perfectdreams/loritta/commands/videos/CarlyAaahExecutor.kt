package net.perfectdreams.loritta.commands.videos

import net.perfectdreams.loritta.commands.images.base.GabrielaImageServerSingleCommandBase
import net.perfectdreams.loritta.commands.images.base.SingleImageOptions
import net.perfectdreams.loritta.common.commands.declarations.CommandExecutorDeclaration
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.utils.gabrielaimageserver.GabrielaImageServerClient

class CarlyAaahExecutor(
    emotes: Emotes,
    client: GabrielaImageServerClient
) : GabrielaImageServerSingleCommandBase(
    emotes,
    client,
    "/api/v1/videos/carly-aaah",
    "carly_aaah.mp4"
) {
    companion object : CommandExecutorDeclaration(CarlyAaahExecutor::class) {
        override val options = SingleImageOptions
    }
}