package net.perfectdreams.loritta.common.builder

import net.perfectdreams.loritta.common.entities.LorittaEmbed
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.User

class MessageBuilder {
    var content: String? = null
    var embed: LorittaEmbed? = null
    // There isn't a multiplatform input stream (sad)
    var files = mutableMapOf<String, ByteArray>()
    var isEphemeral = false
    var allowedMentions = AllowedMentionsBuilder()

    /**
     * Mentions a [user] in this message
     *
     * @param user         the user that will be mentioned
     * @param notifyUser if the user should be notified about the message
     * @return the user mention
     */
    fun mentionUser(user: User, notifyUser: Boolean = true): String {
        if (notifyUser)
            allowedMentions.users.add(user)

        return user.asMention
    }

    fun addFile(fileName: String, stream: ByteArray) {
        files[fileName] = stream
    }

    fun build() = LorittaMessage(
        content ?: " ",
        embed,
        files,
        isEphemeral,
        allowedMentions.build()
    )
}