package net.perfectdreams.loritta.morenitta.interactions

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import java.util.*

abstract class InteractionContext(
    loritta: LorittaBot,
    config: ServerConfig,
    lorittaUser: LorittaUser,
    locale: BaseLocale,
    i18nContext: I18nContext,
    mentions: UnleashedMentions,
    private val replyCallback: IReplyCallback,
) : UnleashedContext(
    loritta,
    config,
    lorittaUser,
    locale,
    i18nContext,
    replyCallback.userLocale,
    replyCallback.guildLocale,
    replyCallback.jda,
    mentions,
    replyCallback.user,
    replyCallback.member,
    replyCallback.guild,
    replyCallback.messageChannel
) {
    override suspend fun deferChannelMessage(ephemeral: Boolean): UnleashedHook.InteractionHook {
        val hook = replyCallback.deferReply().setEphemeral(ephemeral).await()
        wasInitiallyDeferredEphemerally = ephemeral
        return UnleashedHook.InteractionHook(hook)
    }

    override suspend fun reply(ephemeral: Boolean, builder: suspend InlineMessage<MessageCreateData>.() -> Unit): InteractionMessage {
        val createdMessage = InlineMessage(MessageCreateBuilder()).apply {
            // Don't let ANY mention through, you can still override the mentions in the builder
            allowedMentionTypes = EnumSet.of(
                Message.MentionType.CHANNEL,
                Message.MentionType.EMOJI,
                Message.MentionType.SLASH_COMMAND
            )

            builder()
        }.build()

        // We could actually disable the components when their state expires, however this is hard to track due to "@original" or ephemeral messages not having an ID associated with it
        // So, if the message is edited, we don't know if we *can* disable the components when their state expires!
        return if (replyCallback.isAcknowledged) {
            val message = replyCallback.hook.sendMessage(createdMessage).setEphemeral(ephemeral).await()
            InteractionMessage.FollowUpInteractionMessage(message)
        } else {
            val hook = replyCallback.reply(createdMessage).setEphemeral(ephemeral).await()
            wasInitiallyDeferredEphemerally = ephemeral
            InteractionMessage.InitialInteractionMessage(hook)
        }
    }
}