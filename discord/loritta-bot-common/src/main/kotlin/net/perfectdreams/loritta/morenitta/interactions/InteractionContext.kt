package net.perfectdreams.loritta.morenitta.interactions

import dev.minn.jda.ktx.messages.InlineMessage
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.callbacks.IReplyCallback
import net.dv8tion.jda.api.interactions.components.ActionComponent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
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
    val loritta: LorittaBot,
    val config: ServerConfig,
    var lorittaUser: LorittaUser,
    val locale: BaseLocale,
    val i18nContext: I18nContext,
) {
    abstract val event: IReplyCallback
    val guildId
        get() = event.guild?.idLong

    val user
        get() = event.user

    suspend fun deferChannelMessage(ephemeral: Boolean): InteractionHook = event.deferReply().setEphemeral(ephemeral).await()

    suspend inline fun reply(ephemeral: Boolean, content: String) = reply(ephemeral) {
        this.content = content
    }

    suspend inline fun reply(ephemeral: Boolean, builder: InlineMessage<MessageCreateData>.() -> Unit = {}) {
        val createdMessage = InlineMessage(MessageCreateBuilder()).apply(builder).build()

        if (event.isAcknowledged) {
            val message = event.hook.sendMessage(createdMessage).setEphemeral(ephemeral).await()
            if (message.components.isEmpty())
                return

            loritta.interactivityManager.launch {
                val components = message.components
                for (componentLayout in components) {
                    for (button in componentLayout.buttons) {
                        val buttonId = button.id?.let { UUID.fromString(it) } ?: continue
                        // Invalidate all button callbacks
                        loritta.interactivityManager.buttonInteractionCallbacks.remove(buttonId)
                    }
                }

                // Disable the components of the message EXCEPT if it is a link button
                message.editMessageComponents(
                    message.components
                        .map {
                            ActionRow.of(
                                it.map { c ->
                                    if (c is ActionComponent && (c is Button && c.style != ButtonStyle.LINK))
                                        c.withDisabled(true)
                                    else
                                        c
                                }
                            )
                        }
                ).await()
            }
        } else {
            event.reply(createdMessage).setEphemeral(ephemeral).await()
            if (createdMessage.components.isEmpty())
                return

            loritta.interactivityManager.launch {
                event.hook.retrieveOriginal()
                    .await()
                    .also { message ->
                        val components = message.components
                        for (componentLayout in components) {
                            for (button in componentLayout.buttons) {
                                val buttonId = button.id?.let { UUID.fromString(it) } ?: continue
                                // Invalidate all button callbacks
                                loritta.interactivityManager.buttonInteractionCallbacks.remove(buttonId)
                            }
                        }

                        // Disable the components of the message EXCEPT if it is a link button
                        event.hook.editOriginalComponents(
                            message.components
                                .map {
                                    ActionRow.of(
                                        it.map { c ->
                                            if (c is ActionComponent && (c is Button && c.style != ButtonStyle.LINK))
                                                c.withDisabled(true)
                                            else
                                                c
                                        }
                                    )
                                }
                        ).await()
                    }
            }
        }
    }
}