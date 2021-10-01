package net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.exceptions.ImageNotFoundException
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.InteractionContext

suspend fun <R> GabrielaImageServerClient.handleExceptions(context: InteractionContext, block: suspend net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient.() -> (R)): R {
    return try {
        block.invoke(this)
    } catch (e: ImageNotFoundException) { // This is called if the image wasn't found
        context.fail(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
    }
    // TODO: More exception handling
}