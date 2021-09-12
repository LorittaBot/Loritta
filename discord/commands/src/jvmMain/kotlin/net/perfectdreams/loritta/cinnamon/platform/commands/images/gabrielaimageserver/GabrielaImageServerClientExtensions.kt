package net.perfectdreams.loritta.cinnamon.platform.commands.images.gabrielaimageserver

import kotlinx.serialization.json.JsonObject
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.GabrielaImageServerClient
import net.perfectdreams.loritta.cinnamon.common.utils.gabrielaimageserver.NoValidImageFoundException
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData

// This is in a extension to avoid cluttering GabrielaImageServerClient with Loritta-related stuff
// (So in the future we can move GISClient to a different project)
suspend fun GabrielaImageServerClient.executeAndHandleExceptions(context: ApplicationCommandContext, endpoint: String, body: JsonObject): ByteArray {
    return try {
        execute(endpoint, body)
    } catch (e: NoValidImageFoundException) { // This is called if the image wasn't valid/untrusted URL/etc
        context.fail(context.i18nContext.get(I18nKeysData.Commands.NoValidImageFound), Emotes.LoriSob)
    } // Because we aren't catching "Exception", the exception should be propagated to the caller!
}