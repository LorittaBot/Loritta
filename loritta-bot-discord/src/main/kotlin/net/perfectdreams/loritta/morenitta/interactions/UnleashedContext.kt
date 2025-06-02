package net.perfectdreams.loritta.morenitta.interactions

import dev.minn.jda.ktx.messages.InlineMessage
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.interactions.Interaction
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.AchievementUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.interactions.commands.CommandException
import net.perfectdreams.loritta.morenitta.utils.LorittaUser

abstract class UnleashedContext(
    val loritta: LorittaBot,
    val config: ServerConfig,
    var lorittaUser: LorittaUser,
    val locale: BaseLocale,
    val i18nContext: I18nContext,
    val discordGuildLocale: DiscordLocale?,
    val discordUserLocale: DiscordLocale,
    val jda: JDA,
    val mentions: UnleashedMentions,
    val user: User,
    val memberOrNull: Member?,
    val guildOrNull: Guild?,
    val channelOrNull: MessageChannel?,
    val discordInteractionOrNull: Interaction? = null
) {
    var alwaysEphemeral = false

    val guildId
        get() = guildOrNull?.idLong

    val guild: Guild
        get() = guildOrNull ?: error("This interaction was not sent in a guild!")

    val member: Member
        get() = memberOrNull ?: error("This interaction was not sent in a guild!")

    val channel: MessageChannel
        get() = channelOrNull ?: error("This interaction was not sent in a message channel!")

    val discordInteraction: Interaction
        get() = discordInteractionOrNull ?: error("This is not executed by an interaction!")

    var wasInitiallyDeferredEphemerally: Boolean? = null

    abstract suspend fun deferChannelMessage(ephemeral: Boolean): UnleashedHook

    suspend fun reply(ephemeral: Boolean, content: String) = reply(ephemeral) {
        this.content = content
    }

    abstract suspend fun reply(ephemeral: Boolean, builder: suspend InlineMessage<MessageCreateData>.() -> Unit = {}): InteractionMessage

    suspend fun chunkedReply(ephemeral: Boolean, builder: suspend ChunkedMessageBuilder.() -> Unit = {}) {
        // Chunked replies are replies that are chunked into multiple messages, depending on the length of the content
        val createdMessage = ChunkedMessageBuilder().apply {
            builder()
        }

        val currentContent = StringBuilder()
        val messages = mutableListOf<InlineMessage<MessageCreateData>.() -> Unit>()

        for (line in createdMessage.content.lines()) {
            if (currentContent.length + line.length + 1 > 2000) {
                // Because this is a callback and that is invoked later, we need to do this at this way
                val currentContentAsString = currentContent.toString()
                messages.add {
                    this.content = currentContentAsString
                }
                currentContent.clear()
            }
            currentContent.append(line)
            currentContent.append("\n")
        }

        if (currentContent.isNotEmpty()) {
            val currentContentAsString = currentContent.toString()
            messages.add {
                this.content = currentContentAsString
            }
        }

        // TODO: Append anything else (components, files, etc) to the last message
        for (message in messages) {
            reply(ephemeral, message)
        }
    }

    /**
     * Throws a [CommandException] with a specific message [block], halting command execution
     *
     * @param reply the message that will be sent
     * @see fail
     * @see CommandException
     */
    fun fail(ephemeral: Boolean, text: String, emote: Emote): Nothing = throw CommandException(ephemeral) {
        styled(text, emote)
    }

    /**
     * Throws a [CommandException] with a specific message [block], halting command execution
     *
     * @param reply the message that will be sent
     * @see fail
     * @see CommandException
     */
    fun fail(ephemeral: Boolean, text: String, emote: String = Emotes.Error.asMention): Nothing = throw CommandException(ephemeral) {
        styled(text, emote)
    }

    /**
     * Throws a [CommandException] with a specific message [block], halting command execution
     *
     * @param reply the message that will be sent
     * @see fail
     * @see CommandException
     */
    fun fail(ephemeral: Boolean, builder: InlineMessage<*>.() -> Unit = {}): Nothing = throw CommandException(ephemeral) {
        builder()
    }

    /**
     * Gives an achievement to the [user] if they don't have it yet.
     *
     * If the user receives an achievement, they will receive an ephemeral message talking about the new achievement.
     *
     * @param type       what achievement should be given
     * @param achievedAt when the achievement was achieved, default is now
     */
    suspend fun giveAchievementAndNotify(type: AchievementType, ephemeral: Boolean, achievedAt: Instant = Clock.System.now())
            = giveAchievementAndNotify(this.user, type, ephemeral, achievedAt)

    /**
     * Notifies [user] about an achievement.
     *
     * @param type       what achievement should be given
     * @param achievedAt when the achievement was achieved, default is now
     */
    suspend fun notifyAchievement(type: AchievementType, ephemeral: Boolean)
            = AchievementUtils.notifyUserAboutAchievement(loritta, this, i18nContext, UserSnowflake.fromId(user.idLong), type, ephemeral)

    /**
     * Gives an achievement to the [user] if they don't have it yet.
     *
     * If the user receives an achievement, they will receive an ephemeral message talking about the new achievement.
     *
     * @param user       the user that will receive the achievement
     * @param type       what achievement should be given
     * @param achievedAt when the achievement was achieved, default is now
     */
    suspend fun giveAchievementAndNotify(user: User, type: AchievementType, ephemeral: Boolean, achievedAt: Instant = Clock.System.now())
            = AchievementUtils.giveAchievementToUserAndNotifyThem(loritta, this, i18nContext, UserSnowflake.fromId(user.idLong), type, ephemeral, achievedAt)
}