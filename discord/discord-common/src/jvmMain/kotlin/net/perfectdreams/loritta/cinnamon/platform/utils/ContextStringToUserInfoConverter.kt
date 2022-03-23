package net.perfectdreams.loritta.cinnamon.platform.utils

import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.cinnamon.platform.commands.ApplicationCommandContext
import net.perfectdreams.loritta.cinnamon.pudding.data.CachedUserInfo
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId

/**
 * Converts a String, using a CommandContext, to a CachedUserInfo object
 */
object ContextStringToUserInfoConverter {
    suspend fun convert(context: ApplicationCommandContext, input: String): CachedUserInfo? {
        if (input.startsWith("<@") && input.endsWith(">")) {
            // Is a mention... maybe?
            val userId = input.removePrefix("<@")
                .removePrefix("!")
                .removeSuffix(">")
                .toLongOrNull() ?: return null // If the input is not a long, then return the input

            val user = context.interaKTionsContext.data.resolved?.users?.get(Snowflake(userId)) ?: return null // If there isn't any matching user, then return null
            return CachedUserInfo(
                UserId(user.id.value),
                user.name,
                user.discriminator,
                user.avatarHash
            )
        }

        val snowflake = try {
            Snowflake(input)
        } catch (e: NumberFormatException) {
            null
        }

        // If the snowflake is not null, then it *may* be a user ID!
        if (snowflake != null) {
            val cachedUserInfo = context.loritta.getCachedUserInfo(UserId(snowflake.value))
            if (cachedUserInfo != null)
                return cachedUserInfo
        }

        return null
    }
}