package net.perfectdreams.loritta.cinnamon.discord.interactions.commands

import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import net.perfectdreams.discordinteraktions.common.builder.message.MessageBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.allowedMentions

/**
 * Mentions a [user] in this message
 *
 * @param user       the user that will be mentioned
 * @param notifyUser if the user should be notified about the message
 * @return the user mention
 */
fun MessageBuilder.mentionUser(user: User, notifyUser: Boolean = true): String {
    if (notifyUser)
        allowedMentions {
            users.add(user.id)
        }

    return "<@${user.id.value}>" // TODO: Implement "asMention" in Discord InteraKTions
}

/**
 * Mentions a [userId] in this message
 *
 * @param userId     the user ID that will be mentioned
 * @param notifyUser if the user should be notified about the message
 * @return the user mention
 */
fun MessageBuilder.mentionUser(userId: Snowflake, notifyUser: Boolean = true): String {
    if (notifyUser)
        allowedMentions {
            users.add(userId)
        }

    return "<@${userId.value}>" // TODO: Implement "asMention" in Discord InteraKTions
}