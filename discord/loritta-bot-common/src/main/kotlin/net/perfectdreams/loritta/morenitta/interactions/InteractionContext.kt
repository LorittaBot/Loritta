package net.perfectdreams.loritta.morenitta.interactions

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.extensions.await

abstract class InteractionContext(
    val loritta: LorittaBot,
    val config: ServerConfig,
    var lorittaUser: LorittaUser,
    val locale: BaseLocale,
    val i18nContext: I18nContext,
) {
    abstract val event: IReplyCallback
    val guildId
        get() = event.guild?.idLong

    val guildOrNull: Guild?
        get() = event.guild

    val guild: Guild
        get() = guildOrNull ?: error("This interaction was not sent in a guild!")

    val user
        get() = event.user

    val memberOrNull: Member?
        get() = event.member

    val member: Member
        get() = memberOrNull ?: error("This interaction was not sent in a guild!")


    var wasInitiallyDeferredEphemerally: Boolean? = null

    suspend fun deferChannelMessage(ephemeral: Boolean): InteractionHook {
        val hook = event.deferReply().setEphemeral(ephemeral).await()
        wasInitiallyDeferredEphemerally = ephemeral
        return hook
    }

    suspend inline fun reply(ephemeral: Boolean, content: String) = reply(ephemeral) {
        this.content = content
    }

    suspend inline fun reply(ephemeral: Boolean, builder: InlineMessage<MessageCreateData>.() -> Unit = {}) {
        val createdMessage = InlineMessage(MessageCreateBuilder()).apply(builder).build()

        // We could actually disable the components when their state expires, however this is hard to track due to "@original" or ephemeral messages not having an ID associated with it
        // So, if the message is edited, we don't know if we *can* disable the components when their state expires!

        if (event.isAcknowledged) {
            val message = event.hook.sendMessage(createdMessage).setEphemeral(ephemeral).await()
        } else {
            event.reply(createdMessage).setEphemeral(ephemeral).await()
            wasInitiallyDeferredEphemerally = ephemeral
        }
    }

    suspend inline fun chunkedReply(ephemeral: Boolean, builder: ChunkedMessageBuilder.() -> Unit = {}) {
        // Chunked replies are replies that are chunked into multiple messages, depending on the length of the content
        val createdMessage = ChunkedMessageBuilder().apply(builder)

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
}