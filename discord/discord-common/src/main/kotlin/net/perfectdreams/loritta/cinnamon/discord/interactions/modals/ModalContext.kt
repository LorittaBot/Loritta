package net.perfectdreams.loritta.cinnamon.discord.interactions.modals

import dev.kord.rest.builder.message.EmbedBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.allowedMentions
import net.perfectdreams.discordinteraktions.common.builder.message.create.InteractionOrFollowupMessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.entities.User
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.emotes.Emote
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.entities.LorittaReply
import net.perfectdreams.loritta.cinnamon.discord.LorittaCinnamon
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.CommandException
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.EphemeralCommandException
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled

open class ModalContext(
    val loritta: LorittaCinnamon,
    val i18nContext: I18nContext,
    val user: User,
    val interaKTionsContext: net.perfectdreams.discordinteraktions.common.modals.ModalContext
) {
    suspend fun sendMessage(message: String, embed: EmbedBuilder? = null) {
        interaKTionsContext.sendMessage {
            // Disable ALL mentions, to avoid a "@everyone 3.0" moment
            allowedMentions {
                repliedUser = true
            }

            content = message
            if (embed != null)
                embeds = (embeds ?: mutableListOf()).apply { this.add(embed) }
        }
    }

    suspend inline fun sendMessage(block: InteractionOrFollowupMessageCreateBuilder.() -> (Unit)) {
        interaKTionsContext.sendMessage {
            // Disable ALL mentions, to avoid a "@everyone 3.0" moment
            allowedMentions {
                repliedUser = true
            }

            block()
        }
    }

    suspend inline fun sendEphemeralMessage(block: InteractionOrFollowupMessageCreateBuilder.() -> (Unit)) {
        interaKTionsContext.sendEphemeralMessage {
            // Disable ALL mentions, to avoid a "@everyone 3.0" moment
            allowedMentions {
                repliedUser = true
            }

            apply(block)
        }
    }

    suspend fun sendEmbed(message: String = "", embed: EmbedBuilder.() -> Unit) {
        sendMessage(message, EmbedBuilder().apply(embed))
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
    suspend fun sendReply(content: String, prefix: net.perfectdreams.loritta.cinnamon.emotes.Emote, block: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {}) = sendMessage {
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
    suspend fun sendReply(content: String, prefix: String = net.perfectdreams.loritta.cinnamon.emotes.Emotes.DefaultStyledPrefix.asMention, block: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {}) = sendMessage {
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
    suspend fun sendReply(reply: net.perfectdreams.loritta.cinnamon.entities.LorittaReply, block: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {}) = sendMessage {
        styled(reply)

        apply(block)
    }

    /**
     * Sends a Loritta-styled formatted ephemeral message
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param content the content of the message
     * @param prefix  the prefix of the message
     */
    suspend fun sendEphemeralReply(content: String, prefix: net.perfectdreams.loritta.cinnamon.emotes.Emote, block: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {}) = sendEphemeralMessage {
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
    suspend fun sendEphemeralReply(content: String, prefix: String = net.perfectdreams.loritta.cinnamon.emotes.Emotes.DefaultStyledPrefix.asMention, block: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {}) = sendEphemeralMessage {
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
    suspend fun sendEphemeralReply(reply: net.perfectdreams.loritta.cinnamon.entities.LorittaReply, block: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {}) = sendEphemeralMessage {
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
    inline fun fail(content: String, prefix: net.perfectdreams.loritta.cinnamon.emotes.Emote, block: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = fail(
        net.perfectdreams.loritta.cinnamon.entities.LorittaReply(
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
    inline fun fail(content: String, prefix: String = net.perfectdreams.loritta.cinnamon.emotes.Emotes.DefaultStyledPrefix.asMention, block: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = fail(
        net.perfectdreams.loritta.cinnamon.entities.LorittaReply(
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
    inline fun fail(reply: net.perfectdreams.loritta.cinnamon.entities.LorittaReply, block: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = fail {
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
    inline fun fail(block: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = throw CommandException(
        InteractionOrFollowupMessageCreateBuilder(false).apply {
            // Disable ALL mentions, to avoid a "@everyone 3.0" moment
            allowedMentions {
                repliedUser = true
            }

            apply(block)
        }
    )

    /**
     * Throws a [CommandException] with a specific [content] and [prefix], ephemerally, halting command execution
     *
     * @param reply  the message that will be sent
     * @param prefix the reply prefix
     * @param block  the message block, used for customization of the message
     * @see fail
     * @see CommandException
     */
    inline fun failEphemerally(content: String, prefix: net.perfectdreams.loritta.cinnamon.emotes.Emote, block: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = failEphemerally(
        net.perfectdreams.loritta.cinnamon.entities.LorittaReply(
            content, prefix.asMention
        ),
        block
    )

    /**
     * Throws a [CommandException] with a specific [content] and [prefix], ephemerally, halting command execution
     *
     * @param reply the message that will be sent
     * @param block the message block, used for customization of the message
     * @see fail
     * @see CommandException
     */
    inline fun failEphemerally(content: String, prefix: String = net.perfectdreams.loritta.cinnamon.emotes.Emotes.DefaultStyledPrefix.asMention, block: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = failEphemerally(
        net.perfectdreams.loritta.cinnamon.entities.LorittaReply(
            content, prefix
        ),
        block
    )

    /**
     * Throws a [CommandException] with a specific [reply], ephemerally, halting command execution
     *
     * @param reply the message that will be sent
     * @param block the message block, used for customization of the message
     * @see fail
     * @see CommandException
     */
    inline fun failEphemerally(reply: net.perfectdreams.loritta.cinnamon.entities.LorittaReply, block: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = failEphemerally {
        styled(reply)
        apply(block)
    }

    /**
     * Throws a [CommandException] with a specific message [block], ephemerally, halting command execution
     *
     * @param reply the message that will be sent
     * @see fail
     * @see CommandException
     */
    inline fun failEphemerally(block: InteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = throw EphemeralCommandException(
        InteractionOrFollowupMessageCreateBuilder(true).apply {
            // Disable ALL mentions, to avoid a "@everyone 3.0" moment
            allowedMentions {
                repliedUser = true
            }

            apply(block)
        }
    )

}