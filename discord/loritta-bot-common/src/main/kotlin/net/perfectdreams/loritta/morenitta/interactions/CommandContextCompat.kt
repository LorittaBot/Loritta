package net.perfectdreams.loritta.morenitta.interactions

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.UserSnowflake
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.AchievementUtils
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.interactions.commands.ApplicationCommandContext
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext

/**
 * A command context that provides compatibility with slash commands and message commands, for ease of migration while maintaining both commands.
 *
 * Ephemeral message state is ignored when using it with normal non-interactions commands. Don't use it to show sensitive information!
 */
interface CommandContextCompat {
    val user: User
    val config: ServerConfig
    val guild: Guild
    val locale: BaseLocale
    val i18nContext: I18nContext
    val messageChannel: MessageChannel
    val loritta: LorittaBot

    suspend fun reply(ephemeral: Boolean, builder: suspend InlineMessage<MessageCreateData>.() -> Unit = {}): InteractionMessage
    suspend fun giveAchievementAndNotify(achievementType: AchievementType, achievedAt: Instant = Clock.System.now())

    class InteractionsCommandContextCompat(val context: ApplicationCommandContext) : CommandContextCompat {
        override val user: User
            get() = context.user

        override val guild: Guild
            get() = context.guild

        override val config: ServerConfig
            get() = context.config

        override val locale: BaseLocale
            get() = context.locale

        override val i18nContext: I18nContext
            get() = context.i18nContext

        override val messageChannel: MessageChannel
            get() = (context.event.channel as? MessageChannel) ?: error("Command wasn't used in a message channel!")

        override val loritta: LorittaBot
            get() = context.loritta

        override suspend fun reply(ephemeral: Boolean, builder: suspend InlineMessage<MessageCreateData>.() -> Unit): InteractionMessage {
            return context.reply(
                ephemeral
            ) {
                // We need to do this because "builder" is suspendable, because we can't inline this function due to it being in an interface
                builder()
            }
        }

        override suspend fun giveAchievementAndNotify(achievementType: AchievementType, achievedAt: Instant) {
            AchievementUtils.giveAchievementToUserAndNotifyThem(
                context.loritta,
                this,
                context.i18nContext,
                UserSnowflake.fromId(user.idLong),
                achievementType,
                achievedAt
            )
        }
    }

    class LegacyDiscordCommandContextCompat(val context: DiscordCommandContext) : CommandContextCompat {
        override val user: User
            get() = context.user

        override val guild: Guild
            get() = context.guild

        override val config: ServerConfig
            get() = context.serverConfig

        override val locale: BaseLocale
            get() = context.locale

        override val i18nContext: I18nContext
            get() = context.i18nContext

        override val messageChannel: MessageChannel
            get() = context.discordMessage.channel

        override val loritta: LorittaBot
            get() = context.loritta

        override suspend fun reply(ephemeral: Boolean, builder: suspend InlineMessage<MessageCreateData>.() -> Unit): InteractionMessage {
            val inlineBuilder = MessageCreate {
                // We need to do this because "builder" is suspendable, because we can't inline this function due to it being in an interface
                builder()
            }

            // This isn't a real follow-up interaction message, but we do have the message data, so that's why we are using it
            return InteractionMessage.FollowUpInteractionMessage(context.sendMessage(inlineBuilder))
        }

        override suspend fun giveAchievementAndNotify(achievementType: AchievementType, achievedAt: Instant) {
            AchievementUtils.giveAchievementToUserAndNotifyThem(
                context.loritta,
                this,
                context.i18nContext,
                UserSnowflake.fromId(user.idLong),
                achievementType,
                achievedAt
            )
        }
    }

    class LegacyMessageCommandContextCompat(val context: CommandContext) : CommandContextCompat {
        override val user: User
            get() = context.userHandle

        override val guild: Guild
            get() = context.guild

        override val config: ServerConfig
            get() = context.config

        override val locale: BaseLocale
            get() = context.locale

        override val i18nContext: I18nContext
            get() = context.i18nContext

        override val messageChannel: MessageChannel
            get() = context.message.channel

        override val loritta: LorittaBot
            get() = context.loritta

        override suspend fun reply(ephemeral: Boolean, builder: suspend InlineMessage<MessageCreateData>.() -> Unit): InteractionMessage {
            val inlineBuilder = MessageCreate {
                // We need to do this because "builder" is suspendable, because we can't inline this function due to it being in an interface
                builder()
            }

            // This isn't a real follow-up interaction message, but we do have the message data, so that's why we are using it
            return InteractionMessage.FollowUpInteractionMessage(context.sendMessage(inlineBuilder))
        }

        override suspend fun giveAchievementAndNotify(achievementType: AchievementType, achievedAt: Instant) {
            AchievementUtils.giveAchievementToUserAndNotifyThem(
                context.loritta,
                this,
                context.i18nContext,
                UserSnowflake.fromId(user.idLong),
                achievementType,
                achievedAt
            )
        }
    }
}