package net.perfectdreams.loritta.common.commands

import net.perfectdreams.loritta.common.LorittaBot
import net.perfectdreams.loritta.common.builder.MessageBuilder
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.Emotes
import net.perfectdreams.loritta.common.entities.AllowedMentions
import net.perfectdreams.loritta.common.entities.LorittaEmbed
import net.perfectdreams.loritta.common.entities.LorittaMessage
import net.perfectdreams.loritta.common.entities.LorittaReply
import net.perfectdreams.loritta.common.entities.MessageChannel
import net.perfectdreams.loritta.common.entities.User
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.embed.EmbedBuilder

abstract class CommandContext(
    // Nifty trick: By keeping it "open", implementations can override this variable.
    // By doing this, classes can use their own platform implementation (example: LorittaDiscord instead of LorittaBot)
    // If you don't keep it "open", the type will always be "LorittaBot", which sucks.
    open val loritta: LorittaBot,
    val locale: BaseLocale,
    val user: User,
    open val channel: MessageChannel
) {
    suspend fun sendMessage(message: String, embed: LorittaEmbed? = null) {
        channel.sendMessage(
            LorittaMessage(
                message,
                listOf(),
                embed,
                emptyMap(),
                isEphemeral = false,
                AllowedMentions(setOf(), true),
                impersonation = null
            )
        )
    }

    suspend fun sendMessage(block: MessageBuilder.() -> (Unit)) = channel.sendMessage(block)

    suspend fun sendEmbed(message: String = "", embed: EmbedBuilder.() -> Unit) {
        sendMessage(message, EmbedBuilder().apply(embed).build())
    }

    /**
     * Sends a Loritta-styled formatted messag
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param content the content of the message
     * @param prefix  the prefix of the message
     */
    suspend fun sendReply(content: String, prefix: Emote, block: MessageBuilder.() -> Unit = {}) = sendMessage {
        styled(content, prefix)

        apply(block)
    }

    /**
     * Sends a Loritta-styled formatted message
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param content the content of the message
     * @param prefix  the prefix of the message
     */
    suspend fun sendReply(content: String, prefix: String = Emotes.defaultStyledPrefix.asMention, block: MessageBuilder.() -> Unit = {}) = sendMessage {
        styled(content, prefix)

        apply(block)
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