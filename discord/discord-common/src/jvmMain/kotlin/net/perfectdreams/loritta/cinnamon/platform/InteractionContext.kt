package net.perfectdreams.loritta.cinnamon.platform

import dev.kord.rest.builder.message.EmbedBuilder
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.perfectdreams.discordinteraktions.api.entities.User
import net.perfectdreams.discordinteraktions.common.builder.message.allowedMentions
import net.perfectdreams.discordinteraktions.common.builder.message.create.EphemeralInteractionOrFollowupMessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.builder.message.create.PublicInteractionOrFollowupMessageCreateBuilder
import net.perfectdreams.discordinteraktions.common.context.InteractionContext
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.common.emotes.Emote
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.entities.LorittaReply
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.platform.commands.CommandException
import net.perfectdreams.loritta.cinnamon.platform.commands.EphemeralCommandException
import net.perfectdreams.loritta.cinnamon.platform.commands.styled
import net.perfectdreams.loritta.cinnamon.platform.utils.getOrCreateUserProfile

open class InteractionContext(
    val loritta: LorittaCinnamon,
    val i18nContext: I18nContext,
    val user: User,
    // Nifty trick: By keeping it "open", implementations can override this variable.
    // By doing this, classes can use their own platform implementation (example: LorittaDiscord instead of LorittaBot)
    // If you don't keep it "open", the type will always be "LorittaBot", which sucks.
    open val interaKTionsContext: InteractionContext
) {
    /**
     * Defers the application command request message with a public message
     */
    suspend fun deferChannelMessage() = interaKTionsContext.deferChannelMessage()

    /**
     * Defers the application command request message with a ephemeral message
     */
    suspend fun deferChannelMessageEphemerally() = interaKTionsContext.deferChannelMessageEphemerally()

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

    suspend fun sendMessage(block: PublicInteractionOrFollowupMessageCreateBuilder.() -> (Unit)) {
        interaKTionsContext.sendMessage {
            // Disable ALL mentions, to avoid a "@everyone 3.0" moment
            allowedMentions {
                repliedUser = true
            }

            apply(block)
        }
    }

    suspend fun sendEphemeralMessage(block: EphemeralInteractionOrFollowupMessageCreateBuilder.() -> (Unit)) {
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
     * Sends a Loritta-styled formatted messag
     *
     * By default, Loritta-styled formatting looks like this: `[prefix] **|** [content]`, however implementations can change the look and feel of the message.
     *
     * Prefixes should *not* be used for important behavior of the command!
     *
     * @param content the content of the message
     * @param prefix  the prefix of the message
     */
    suspend fun sendReply(content: String, prefix: Emote, block: PublicInteractionOrFollowupMessageCreateBuilder.() -> Unit = {}) = sendMessage {
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
    suspend fun sendReply(content: String, prefix: String = Emotes.DefaultStyledPrefix.asMention, block: PublicInteractionOrFollowupMessageCreateBuilder.() -> Unit = {}) = sendMessage {
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
    suspend fun sendReply(reply: LorittaReply, block: PublicInteractionOrFollowupMessageCreateBuilder.() -> Unit = {}) = sendMessage {
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
    fun fail(content: String, prefix: Emote, block: PublicInteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = fail(
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
    fun fail(content: String, prefix: String = Emotes.DefaultStyledPrefix.asMention, block: PublicInteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = fail(
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
    fun fail(reply: LorittaReply, block: PublicInteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = fail {
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
    fun fail(block: PublicInteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = throw CommandException {
        // Disable ALL mentions, to avoid a "@everyone 3.0" moment
        allowedMentions {
            repliedUser = true
        }

        apply(block)
    }

    /**
     * Throws a [CommandException] with a specific [content] and [prefix], ephemerally, halting command execution
     *
     * @param reply  the message that will be sent
     * @param prefix the reply prefix
     * @param block  the message block, used for customization of the message
     * @see fail
     * @see CommandException
     */
    fun failEphemerally(content: String, prefix: Emote, block: EphemeralInteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = failEphemerally(
        LorittaReply(
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
    fun failEphemerally(content: String, prefix: String = Emotes.DefaultStyledPrefix.asMention, block: EphemeralInteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = failEphemerally(
        LorittaReply(
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
    fun failEphemerally(reply: LorittaReply, block: EphemeralInteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = failEphemerally {
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
    fun failEphemerally(block: EphemeralInteractionOrFollowupMessageCreateBuilder.() -> Unit = {}): Nothing = throw EphemeralCommandException {
        // Disable ALL mentions, to avoid a "@everyone 3.0" moment
        allowedMentions {
            repliedUser = true
        }

        apply(block)
    }

    /**
     * Gives an achievement to the [user] if they don't have it yet.
     *
     * If the user receives an achievement, they will receive an ephemeral message talking about the new achievement.
     *
     * @param type       what achievement should be given
     * @param achievedAt when the achievement was achieved, default is now
     */
    suspend fun giveAchievement(type: AchievementType, achievedAt: Instant = Clock.System.now()) {
        val profile = loritta.services.users.getOrCreateUserProfile(user)
        val wasAchievementGiven = profile.giveAchievement(
            type,
            achievedAt
        )

        if (wasAchievementGiven)
            sendEphemeralMessage {
                styled(
                    content = "**${i18nContext.get(I18nKeysData.Achievements.AchievementUnlocked)}**",
                    prefix = Emotes.Sparkles
                )

                styled(
                    "**${i18nContext.get(type.title)}:** ${i18nContext.get(type.description)}",
                    prefix = type.category.emote
                )
            }
    }
}