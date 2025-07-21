package net.perfectdreams.loritta.morenitta.interactions.commands

import com.github.kevinsawicki.http.HttpRequest
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageCreate
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.exceptions.PermissionException
import net.dv8tion.jda.api.interactions.DiscordLocale
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
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
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.LorittaUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getEffectiveAvatarUrl
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
    companion object {
        private val logger by HarmonyLoggerFactory.logger {}
    }

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

    /**
     * Based on AbstractCommand's context that tries a bunch of different things to get the first image.
     */
    suspend fun imageUrlAt(index: Int, search: Int = 25, avatarSize: Int = 256): String? {
        if (args.size > index) {
            // First: a common link
            val link = args[index]

            if (LorittaUtils.isValidUrl(link) && loritta.connectionManager.isTrusted(link)) {
                logger.info { "Found $link in command arguments, returning it." }
                return link
            }

            // Second: a user avatar url
            val user = getUser(index)

            if (user != null) {
                logger.info { "User mentioned someone, getting their avatar and retuning it." }

                return user.getEffectiveAvatarUrl(ImageFormat.PNG, avatarSize)
            }

            // Third: it may be an emoji
            for (emote in event.message.mentions.customEmojis) {
                if (link.equals(emote.asMention, true)) {
                    logger.info { "User used an emoji, getting its url and returning it." }
                    return emote.imageUrl
                }
            }

            // Fourth: image inside an embed...
            for (embed in event.message.embeds) {
                if (embed.image != null && loritta.connectionManager.isTrusted(embed.image!!.url!!)) {
                    logger.info { "Found an image inside an embed, getting its url and returning it." }

                    return embed.image!!.url!!
                }
            }

            // Fifth: an attachment...
            for (attachment in event.message.attachments) {
                if (attachment != null && loritta.connectionManager.isTrusted(attachment.url)) {
                    logger.info { "Found an image attachment, getting its url and returning it." }

                    return attachment.url
                }
            }

            // If we got here, it could be a standard discord emoji (twemoji)
            try {
                var unicodeEmoji = LorittaUtils.toUnicode(args[index].codePointAt(0))
                unicodeEmoji = unicodeEmoji.substring(2)
                val toBeDownloaded = "https://abs.twimg.com/emoji/v2/72x72/$unicodeEmoji.png"
                if (HttpRequest.get(toBeDownloaded).code() == 200) {
                    logger.info { "Found an unicode emote, getting its url and returning it." }
                    return toBeDownloaded
                }
            } catch (_: Exception) {
            }
        }

        // Nothing yet? Maybe the replied message content has something
        if (!event.isFromType(ChannelType.PRIVATE) && guild.selfMember.hasPermission(event.channel as GuildChannel, Permission.MESSAGE_HISTORY)) {
            val referencedMessage = event.message.referencedMessage

            if (referencedMessage != null) {
                for (embed in referencedMessage.embeds) {
                    if (embed.image != null && loritta.connectionManager.isTrusted(embed.image!!.url!!)) {
                        logger.info { "Found an image in referenced message embed, returning its url." }

                        return embed.image!!.url!!
                    }
                }
                for (attachment in referencedMessage.attachments) {
                    if (attachment.isImage && loritta.connectionManager.isTrusted(attachment.url)) {
                        logger.info { "Found an image attachment in referenced message, returning its url." }

                        return attachment.url
                    }
                }
            }
        }

        // Ok, at this point we're just going through the old messages in the chat
        if (search > 0 && !event.isFromType(ChannelType.PRIVATE) && guild.selfMember.hasPermission(event.channel as GuildChannel, Permission.MESSAGE_HISTORY)) {
            try {
                val messages = event.message.channel.history.retrievePast(search).await()

                attach@ for (msg in messages) {
                    for (embed in msg.embeds) {
                        if (embed.image != null && loritta.connectionManager.isTrusted(embed.image!!.url!!)) {
                            logger.info { "Found an image inside of past message '${msg.id}' embed, returning its url." }

                            return embed.image!!.url!!
                        }
                    }

                    for (attachment in msg.attachments) {
                        if (attachment.isImage && loritta.connectionManager.isTrusted(attachment.url)) {
                            logger.info { "Found an image attachment inside of past message '${msg.id}', returning its url." }

                            return attachment.url
                        }
                    }
                }
            } catch (_: PermissionException) {
            }
        }

        return null
    }

    fun getImage(index: Int) = event.message.attachments.getOrNull(index)

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