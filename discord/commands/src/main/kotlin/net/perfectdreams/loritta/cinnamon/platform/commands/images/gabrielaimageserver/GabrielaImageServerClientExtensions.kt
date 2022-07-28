package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.images.gabrielaimageserver

import net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient
import net.perfectdreams.gabrielaimageserver.exceptions.ContentLengthTooLargeException
import net.perfectdreams.gabrielaimageserver.exceptions.ImageNotFoundException
import net.perfectdreams.gabrielaimageserver.exceptions.ImageTooLargeException
import net.perfectdreams.gabrielaimageserver.exceptions.StreamExceedsLimitException
import net.perfectdreams.gabrielaimageserver.exceptions.UntrustedURLException
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.discord.InteractionContext

suspend fun <R> GabrielaImageServerClient.handleExceptions(context: InteractionContext, block: suspend net.perfectdreams.gabrielaimageserver.client.GabrielaImageServerClient.() -> (R)): R {
    return try {
        block.invoke(this)
    } catch (e: Exception) { // This is called if the image wasn't found
        when (e) {
            is ImageNotFoundException -> context.fail(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
            is UntrustedURLException -> context.fail(context.i18nContext.get(I18nKeysData.Commands.ImageUrlIsUntrusted), Emotes.LoriSob)
            is ImageTooLargeException, is StreamExceedsLimitException, is ContentLengthTooLargeException -> context.fail(context.i18nContext.get(I18nKeysData.Commands.SentImageIsTooLarge), Emotes.LoriSob)
            else -> throw e // Propagate it
        }
    }
    // TODO: More exception handling
}