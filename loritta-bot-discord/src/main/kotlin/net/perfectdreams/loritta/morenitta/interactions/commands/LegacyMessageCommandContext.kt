package net.perfectdreams.loritta.morenitta.interactions.commands

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.emoji.CustomEmoji
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.text.TextUtils.convertMarkdownLinksWithLabelsToPlainLinks
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.interactions.InteractionMessage
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.UnleashedHook
import net.perfectdreams.loritta.morenitta.interactions.UnleashedMentions
import net.perfectdreams.loritta.morenitta.interactions.commands.options.UserAndMember
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.referenceIfPossible
import java.util.*

/**
 * A command context that provides compatibility with legacy message commands.
 *
 * Ephemeral message state is ignored when using it with normal non-interactions commands. Don't use it to show sensitive information!
 */
class LegacyMessageCommandContext(
    loritta: LorittaBot,
    config: ServerConfig,
    lorittaUser: LorittaUser,
    locale: BaseLocale,
    i18nContext: I18nContext,
    val event: LorittaMessageEvent,
    val args: List<String>,
    override val rootDeclaration: SlashCommandDeclaration,
    override val commandDeclaration: SlashCommandDeclaration
) : UnleashedContext(
    loritta,
    config,
    lorittaUser,
    locale,
    i18nContext,
    // TODO: Convert the current locale/i18nContext into a DiscordLocale
    DiscordLocale.PORTUGUESE_BRAZILIAN,
    DiscordLocale.PORTUGUESE_BRAZILIAN,
    event.jda,
    UnleashedMentions(
        event.message.mentions.users,
        event.message.mentions.channels,
        event.message.mentions.customEmojis,
        event.message.mentions.roles
    ),
    event.author,
    event.member,
    event.guild,
    event.channel
), CommandContext {
    override suspend fun deferChannelMessage(ephemeral: Boolean): UnleashedHook.LegacyMessageHook {
        // Message commands do not have deferring like slash commands...
        // But instead of doing just a noop, we can get cheeky with it, heh
        event.channel.sendTyping().queue()

        return UnleashedHook.LegacyMessageHook()
    }

    override suspend fun reply(
        ephemeral: Boolean,
        builder: suspend InlineMessage<MessageCreateData>.() -> Unit
    ): InteractionMessage {
        val inlineBuilder = MessageCreate {
            // Don't let ANY mention through, you can still override the mentions in the builder
            allowedMentionTypes = EnumSet.of(
                Message.MentionType.CHANNEL,
                Message.MentionType.EMOJI,
                Message.MentionType.SLASH_COMMAND
            )

            // We need to do this because "builder" is suspendable, because we can't inline this function due to it being in an interface
            builder()

            // We are going to replace any links with labels with just links, since Discord does not support labels with links if it isn't a webhook or an interaction
            content = content?.convertMarkdownLinksWithLabelsToPlainLinks()
        }

        // This isn't a real follow-up interaction message, but we do have the message data, so that's why we are using it
        return InteractionMessage.FollowUpInteractionMessage(
            event.channel.sendMessage(inlineBuilder)
                .referenceIfPossible(event.message)
                .failOnInvalidReply(false)
                .await()
        )
    }

    fun getImage(index: Int) = event.message.attachments.getOrNull(index)

    fun getEmoji(index: Int) = event.message.mentions.customEmojis.getOrNull(index)

    /**
     * Gets a [User] reference from the argument at the specified [index]
     */
    suspend fun getUser(index: Int): User? {
        val arg = args.getOrNull(index) ?: return null

        return DiscordUtils.extractUserFromString(
            loritta,
            arg,
            mentions.users,
            event.guild
        )
    }

    /**
     * Gets a [UserAndMember] reference from the argument at the specified [index]
     */
    suspend fun getUserAndMember(index: Int): UserAndMember? {
        val user = getUser(index) ?: return null

        val member = event.guild?.getMember(user)

        return UserAndMember(
            user,
            member
        )
    }
}