package net.perfectdreams.loritta.cinnamon.discord.commands

import net.perfectdreams.discordinteraktions.api.entities.User
import net.perfectdreams.discordinteraktions.common.builder.message.create.MessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.create.allowedMentions

/**
 * Mentions a [user] in this message
 *
 * @param user         the user that will be mentioned
 * @param notifyUser if the user should be notified about the message
 * @return the user mention
 */
fun MessageCreateBuilder.mentionUser(user: User, notifyUser: Boolean = true): String {
    if (notifyUser)
        allowedMentions {
            users.add(user.id)
        }

    return "<@$user>" // TODO: Implement "asMention" in Discord InteraKTions
}