package net.perfectdreams.loritta.common.utils.gabrielaimageserver

import kotlinx.serialization.json.JsonObject
import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.emotes.Emotes

// This is in a extension to avoid cluttering GabrielaImageServerClient with Loritta-related stuff
// (So in the future we can move GISClient to a different project)
suspend fun GabrielaImageServerClient.executeAndHandleExceptions(context: CommandContext, emotes: Emotes, endpoint: String, body: JsonObject): ByteArray {
    return try {
        execute(endpoint, body)
    } catch (e: NoValidImageFoundException) {
        context.fail(context.locale["commands.noValidImageFound", emotes.loriSob], emotes.loriSob)
    } catch (e: ErrorWhileGeneratingImageException) {
        context.fail(
            context.locale["commands.errorWhileExecutingCommand", emotes.loriRage, emotes.loriSob],
            "\uD83E\uDD37"
        )
    } // Because we aren't catching "Exception", the exception should be propagated to the caller!
}