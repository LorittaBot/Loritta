package net.perfectdreams.loritta.common.builder

import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.entities.LorittaEmbed
import net.perfectdreams.loritta.common.entities.LorittaImpersonation
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.LorittaReply
import net.perfectdreams.loritta.common.entities.Message
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.images.ImageReference
import net.perfectdreams.loritta.common.utils.CinnamonDslMarker
import net.perfectdreams.loritta.common.utils.embed.EmbedBuilder

@CinnamonDslMarker
class MessageBuilder {
    var content: String? = null
    var replies = mutableListOf<LorittaReply>()
    var embed: LorittaEmbed? = null
    // There isn't a multiplatform input stream (sad)
    var files = mutableMapOf<String, ByteArray>()
    var isEphemeral = false
    var allowedMentions = AllowedMentionsBuilder()
    var impersonation: LorittaImpersonation? = null
    private var messageReferenceId: Long? = null

    fun impersonation(username: String, avatar: ImageReference) {
        impersonation = LorittaImpersonation(username, avatar)
    }

    /**
     * Appends a embed to this builder
     *
     * @param embed a embed builder
     */
    fun embed(embed: EmbedBuilder.() -> Unit){
        this.embed = net.perfectdreams.loritta.common.utils.embed.embed(embed).build()
    }

    /**
     * References the [message] in the message
     *
     * Implementations may implement this as a "inline reply", however implementations may ignore this parameter if the platform doesn't allow
     * overriding the referenced message or if the implementation does not support message references.
     *
     * @param message the message that will be referenced
     */
    fun reference(message: Message) {
        messageReferenceId = message.id
    }

    /**
     * Appends a Loritta-styled formatted message to this builder
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param content           the content of the message
     * @param prefix            the prefix of the message
     * @param inReplyToUser     the user that is within the context of this reply
     * @param mentionSenderHint if the user should be mentioned in the reply, implementations may decide to not add the mention if it isn't needed.
     */
    fun styled(content: String, prefix: Emote, inReplyToUser: User? = null, mentionSenderHint: Boolean = false) = styled(content, prefix.asMention, inReplyToUser, mentionSenderHint)

    /**
     * Appends a Loritta-styled formatted message to this builder
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param content           the content of the message
     * @param prefix            the prefix of the message
     * @param inReplyToUser     the user that is within the context of this reply
     * @param mentionSenderHint if the user should be mentioned in the reply, implementations may decide to not add the mention if it isn't needed.
     */
    fun styled(content: String, prefix: String = Emotes.defaultStyledPrefix.asMention, inReplyToUser: User? = null, mentionSenderHint: Boolean = false) = styled(LorittaReply(content, prefix, inReplyToUser, mentionSenderHint))

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
        val replies = replies
        val allowedMentions = allowedMentions

        if (content == null && embed == null && files.isEmpty() && replies.isEmpty())
            throw UnsupportedOperationException("Message needs to have at least content, embed or a file!")

        // Add all replied users that has a mention sender hint to the allowed mentions block
        for (reply in replies) {
            if (reply.mentionSenderHint && reply.inReplyToUser != null) {
                allowedMentions.users.add(reply.inReplyToUser)
            }
        }

        return LorittaMessage(
            content,
            replies,
            embed,
            files,
            isEphemeral,
            allowedMentions.build(),
            impersonation,
            messageReferenceId
        )
    }
}