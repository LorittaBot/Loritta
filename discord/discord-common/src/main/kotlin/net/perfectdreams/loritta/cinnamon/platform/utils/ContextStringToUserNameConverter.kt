package net.perfectdreams.loritta.cinnamon.platform.utils

import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext

/**
 * Converts a String, using a CommandContext, to a User name
 */
object ContextStringToUserNameConverter {
    suspend fun convert(context: ApplicationCommandContext, input: String): String {
        return ContextStringToUserInfoConverter.convert(context, input)?.name ?: input
    }
}