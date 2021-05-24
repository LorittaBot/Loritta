package net.perfectdreams.loritta.common.commands

import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.builder.ContextualMultiReplyBuilder
import net.perfectdreams.loritta.common.builder.MessageBuilder
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.entities.LorittaReply
import net.perfectdreams.loritta.common.entities.Message
import net.perfectdreams.loritta.common.entities.MessageChannel
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.locale.BaseLocale

abstract class CommandContext(
    // Nifty trick: By keeping it "open", implementations can override this variable.
    // By doing this, classes can use their own platform implementation (example: LorittaDiscord instead of LorittaBot)
    // If you don't keep it "open", the type will always be "LorittaBot", which sucks.
    open val loritta: LorittaBot,
    val locale: BaseLocale,
    val user: User,
    val message: Message,
    open val channel: MessageChannel
) {
    suspend fun sendMessage(block: MessageBuilder.() -> (Unit)) = channel.sendMessage(block)

    /**
     * Sends a Loritta-styled formatted messag
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param content the content of the message
     * @param prefix  the prefix of the message
     * @param inReplyToUser     the user that is within the context of this reply
     * @param mentionSenderHint if the user should be mentioned in the reply, implementations may decide to not add the mention if it isn't needed.
     */
    suspend fun sendReply(content: String, prefix: Emote, inReplyToUser: User = user, mentionSenderHint: Boolean = false, block: MessageBuilder.() -> Unit = {})
            = sendReply(content, prefix.asMention, inReplyToUser, mentionSenderHint, block)

    /**
     * Sends a Loritta-styled formatted message
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param content the content of the message
     * @param prefix  the prefix of the message
     * @param mentionSenderHint if the user should be mentioned in the reply, implementations may decide to not add the mention if it isn't needed.
     */
    suspend fun sendReply(content: String, prefix: String = Emotes.defaultStyledPrefix.asMention, inReplyToUser: User = user, mentionSenderHint: Boolean = false, block: MessageBuilder.() -> Unit = {}) = sendMessage {
        styled(content, prefix, inReplyToUser, mentionSenderHint)
        apply(block)

        reference(message)
    }

    /**
     * Sends a Loritta-styled formatted message to this builder
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param reply the already built LorittaReply
     */
    suspend fun sendReply(reply: LorittaReply, block: MessageBuilder.() -> Unit = {}) = sendMessage {
        styled(reply)
        apply(block)

        reference(message)
    }

    /**
     * Sends multiple Loritta-styled formatted messages in a single message
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * This is contextual, it uses the current command context as a context, example: inReplyToUser is automatically set to the [user]
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param block the contextual multi reply builder block
     */
    suspend fun sendReplies(block: ContextualMultiReplyBuilder.() -> Unit = {}) = sendMessage {
        val builder = ContextualMultiReplyBuilder(this@CommandContext).apply(block)
        builder.replies.forEach { styled(it) }

        reference(message)
    }

    /**
     * Throws a [CommandException] with a specific [content] and [prefix], halting command execution
     *
     * @param reply  the message that will be sent
     * @param prefix the reply prefix
     * @param block  the message block, used for customization of the message
     * @see fail
     * @see CommandException
     */
    fun fail(content: String, prefix: Emote, block: MessageBuilder.() -> Unit = {}): Nothing = fail(
        LorittaReply(
            content, prefix.asMention
        ),
        block
    )

    /**
     * Throws a [CommandException] with a specific [content] and [prefix], halting command execution
     *
     * @param reply the message that will be sent
     * @param block the message block, used for customization of the message
     * @see fail
     * @see CommandException
     */
    fun fail(content: String, prefix: String = Emotes.defaultStyledPrefix.asMention, block: MessageBuilder.() -> Unit = {}): Nothing = fail(
        LorittaReply(
            content, prefix
        ),
        block
    )

    /**
     * Throws a [CommandException] with a specific [reply], halting command execution
     *
     * @param reply the message that will be sent
     * @param block the message block, used for customization of the message
     * @see fail
     * @see CommandException
     */
    fun fail(reply: LorittaReply, block: MessageBuilder.() -> Unit = {}): Nothing = fail {
        styled(reply)
        apply(block)
    }

    /**
     * Throws a [CommandException] with a specific message [block], halting command execution
     *
     * @param reply the message that will be sent
     * @see fail
     * @see CommandException
     */
    fun fail(block: MessageBuilder.() -> Unit = {}): Nothing = throw CommandException(
        MessageBuilder().apply(block).build()
    )
}