package net.perfectdreams.loritta.cinnamon.discord.interactions.vanilla.moderation

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.Member
import dev.kord.core.entity.User
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils

object AdminUtils {
    suspend fun checkAndRetrieveAllValidUsersFromString(context: ApplicationCommandContext, usersAsString: String)
            = retrieveAllValidUsersFromString(context, usersAsString)

    suspend fun retrieveAllValidUsersFromString(context: ApplicationCommandContext, usersAsString: String): List<UserQueryResult> {
        val users = mutableListOf<UserQueryResult>()

        // First, we will get all the mentioned users in the usersAsString, as long as they are ResolvedObjects map
        DiscordUtils.USER_MENTION_REGEX.findAll(usersAsString)
            .mapNotNull { it.groupValues[1].toLongOrNull() }
            .map { Snowflake(it) }
            .mapNotNull {
                val user = context.interaKTionsContext.interactionData.resolved?.users?.get(it) ?: return@mapNotNull null
                val member = context.interaKTionsContext.interactionData.resolved?.members?.get(it)

                // We are sure that the member doesn't exist because it wasn't resolved, so we will indicate to the UserQueryResult
                // that we don't need to fetch the member!
                UserQueryResult(member ?: user, member == null)
            }
            .toCollection(users)

        // Now, we will get all the user IDs in the input
        usersAsString.replace(DiscordUtils.USER_MENTION_REGEX, " ")
            .split(" ")
            .asSequence()
            .filter { it.isNotBlank() }
            .mapNotNull { it.toLongOrNull() }
            .map { Snowflake(it) }
            .toList() // We need to have a terminal operator here, because a sequence is not suspendable
            .mapNotNull {
                try {
                    context.loritta.kord.getUser(it)
                } catch (e: Exception) {
                    null
                } // Probably not a valid user
            }
            .map {
                UserQueryResult(it, false)
            }
            .toCollection(users)

        return users
    }

    class UserQueryResult(val user: User, val memberDoesNotExist: Boolean) {
        suspend fun queryMember(guildId: Snowflake) = if (user is Member)
            user
        else if (memberDoesNotExist)
            null
        else
            user.fetchMemberOrNull(guildId)
    }

    data class InteractionCheck(
        val issuer: User,
        val target: User,
        val result: InteractionCheckResult
    )

    enum class InteractionCheckResult {
        SUCCESS,
        TARGET_IS_OWNER,
        TARGET_ROLE_POSITION_HIGHER_OR_EQUAL_TO_ISSUER,
        TRYING_TO_INTERACT_WITH_SELF
    }
}