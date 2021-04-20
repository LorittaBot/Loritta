package net.perfectdreams.loritta.common.builder

import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.utils.CinnamonDslMarker

/**
 * Builds a Loritta Reply, Loritta Replies are a fancy formatting to normal messages, mostly looks like this:
 *
 * Prefix **|** UserMention Content
 */
@CinnamonDslMarker
open class LorittaReplyBuilder {
    var content: String? = null
    var prefix: String? = "\uD83D\uDD39"
    var isEphemeral = false
    var allowedMentions = AllowedMentionsBuilder()

    fun mentionUser(user: User, notifyUser: Boolean = true): String {
        if (notifyUser)
            allowedMentions.users.add(user)

        return user.asMention
    }
}