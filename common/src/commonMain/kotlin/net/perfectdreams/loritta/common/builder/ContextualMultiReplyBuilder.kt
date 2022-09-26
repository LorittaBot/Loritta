package net.perfectdreams.loritta.common.builder

import net.perfectdreams.loritta.common.commands.CommandContext
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.entities.LorittaReply
import net.perfectdreams.loritta.common.entities.User

class ContextualMultiReplyBuilder(private val context: CommandContext) {
    var replies = mutableListOf<LorittaReply>()

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
    fun styled(content: String, prefix: Emote, inReplyToUser: User? = context.user, mentionSenderHint: Boolean = false) = styled(content, prefix.asMention, inReplyToUser, mentionSenderHint)

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
    fun styled(content: String, prefix: String = Emotes.defaultStyledPrefix.asMention, inReplyToUser: User? = context.user, mentionSenderHint: Boolean = false) = styled(LorittaReply(content, prefix, inReplyToUser, mentionSenderHint))

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
}