package net.perfectdreams.loritta.cinnamon.platform.utils

import dev.kord.common.entity.Snowflake
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext

/**
 * Converts a String, using a CommandContext, to a User object
 */
object ContextStringToUserConverter {
    suspend fun convert(context: ApplicationCommandContext, input: String): User? {
        if (input.startsWith("<@") && input.endsWith(">")) {
            // Is a mention... maybe?
            val userId = input.removePrefix("<@")
                .removePrefix("!")
                .removeSuffix(">")
                .toLongOrNull() ?: return null // If the input is not a long, then return the input

            val user = context.interaKTionsContext.data.resolved?.users?.get(Snowflake(userId)) ?: return null // If there isn't any matching user, then return null
            return user
        }

        return null
    }
}