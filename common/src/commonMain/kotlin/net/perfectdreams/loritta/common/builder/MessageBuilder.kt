package net.perfectdreams.loritta.common.builder

import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.entities.LorittaEmbed
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.LorittaReply
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.utils.CinnamonDslMarker

@CinnamonDslMarker
class MessageBuilder {
    var content: String? = null
    var replies = mutableListOf<LorittaReply>()
    var embed: LorittaEmbed? = null
    // There isn't a multiplatform input stream (sad)
    var files = mutableMapOf<String, ByteArray>()
    var isEphemeral = false
    var allowedMentions = AllowedMentionsBuilder()

    /**
     * Appends a Loritta-styled formatted message to this builder
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param content the already built LorittaReply
     */
    fun styled(content: String, prefix: Emote) = styled(content, prefix.asMention)

    /**
     * Appends a Loritta-styled formatted message to this builder
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param content the content of the message
     * @param prefix  the prefix of the message
     */
    fun styled(content: String, prefix: String = Emotes.defaultStyledPrefix.asMention) = styled(LorittaReply(content, prefix))

    /**
     * Appends a Loritta-styled formatted message to this builder
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param content the already built LorittaReply
     */
    fun styled(reply: LorittaReply) = replies.add(reply)

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

    fun build(): LorittaMessage {
        val content = content
        val embed = embed
        val files = files

        if (content == null && embed == null && files.isEmpty() && replies.isEmpty())
            throw UnsupportedOperationException("Message needs to have at least content, embed or a file!")

        return LorittaMessage(
            content,
            replies,
            embed,
            files,
            isEphemeral,
            allowedMentions.build()
        )
    }
}