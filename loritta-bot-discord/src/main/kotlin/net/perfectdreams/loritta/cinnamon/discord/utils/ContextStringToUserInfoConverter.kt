package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.common.entity.Snowflake
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.serializable.CachedUserInfo
import net.perfectdreams.loritta.serializable.UserId

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

            val user = context.interaKTionsContext.interactionData.resolved?.users?.get(Snowflake(userId)) ?: return null // If there isn't any matching user, then return null
            return CachedUserInfo(
                UserId(user.id.value),
                user.username,
                user.discriminator,
                null,
                user.data.avatar
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

    suspend fun convert(context: UnleashedContext, input: String): net.perfectdreams.loritta.morenitta.utils.CachedUserInfo? {
        if (input.startsWith("<@") && input.endsWith(">")) {
            val userId = input.removePrefix("<@")
                .removePrefix("!")
                .removeSuffix(">")
                .toLongOrNull() ?: return null

            return context.loritta.lorittaShards.retrieveUserInfoById(userId)
        }
        return null
    }
}