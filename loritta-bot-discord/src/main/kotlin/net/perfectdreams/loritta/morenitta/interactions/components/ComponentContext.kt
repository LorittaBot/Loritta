package net.perfectdreams.loritta.morenitta.interactions.components

import dev.minn.jda.ktx.interactions.components.asDisabled
import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import dev.minn.jda.ktx.messages.MessageEditBuilder
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.button.Button
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.LoadingEmojis
import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.interactions.*
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalArguments
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalContext
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.toJDA
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Context of the executed command
 */
class ComponentContext(
    loritta: LorittaBot,
    config: ServerConfig,
    lorittaUser: LorittaUser,
    locale: BaseLocale,
    i18nContext: I18nContext,
    val event: ComponentInteraction
) : InteractionContext(loritta, config, lorittaUser, locale, i18nContext, UnleashedMentions(emptyList(), emptyList(), emptyList(), emptyList()), event) {
    suspend fun deferEdit(): InteractionHook = event.deferEdit().await()

    suspend fun deferEditAsync(): CompletableFuture<InteractionHook> = event.deferEdit().submit()

    /**
     * Edits the message that invoked the action
     */
    suspend inline fun editMessage(isReplace: Boolean = false, action: InlineMessage<*>.() -> (Unit)) = editMessage(isReplace, MessageEditBuilder { apply(action) }.build())

    /**
     * Edits the message that invoked the action
     */
    suspend inline fun editMessage(isReplace: Boolean = false, messageEditData: MessageEditData): UnleashedHook {
        return UnleashedHook.InteractionHook(event.editMessage(messageEditData).apply { this.isReplace = isReplace }.await())
    }

    /**
     * Defers the edit with [deferEdit] and edits the message with the result of the [action]
     */
    suspend inline fun deferAndEditOriginal(action: InlineMessage<*>.() -> (Unit)) = deferAndEditOriginal(MessageEditBuilder { apply(action) }.build())

    /**
     * Defers the edit with [deferEdit] and edits the message with the result of the [action]
     */
    suspend inline fun deferAndEditOriginal(messageEditData: MessageEditData): Message {
        val hook = deferEdit()

        return hook.editOriginal(messageEditData).await()
    }

    suspend fun updateMessageSetLoadingState(
        updateMessageContent: Boolean = true,
        disableComponents: Boolean = true,
        loadingEmoji: DiscordEmote = LoadingEmojis.random()
    ): InteractionHook {
        val hook = deferEdit()

        val builtMessage = MessageEdit {
            if (updateMessageContent)
                styled(
                    i18nContext.get(I18nKeysData.Website.Dashboard.Loading),
                    loadingEmoji
                )

            if (disableComponents)
                    event.message.components.asDisabled().forEach {
                        if (it is ActionRow) {
                            this.actionRow(
                                it.components.map {
                                    if (it is Button && event.componentId == it.id)
                                        it.withEmoji(loadingEmoji.toJDA())
                                    else
                                        it
                                }
                            )
                        }
                    }
        }

        hook.editOriginal(builtMessage).await()
        return hook
    }

    /**
     * Edits the message in an async manner
     *
     * The edit is "async" because the job is submitted instead of awaited, useful if you want to edit the message while doing other work in parallel
     */
    inline fun editMessageAsync(action: InlineMessage<*>.() -> (Unit)) = editMessageAsync(MessageEditBuilder { apply(action) }.build())

    /**
     * Edits the message in an async manner
     *
     * The edit is "async" because the job is submitted instead of awaited, useful if you want to edit the message while doing other work in parallel
     */
    fun editMessageAsync(messageEditData: MessageEditData): CompletableFuture<InteractionHook> {
        return event.editMessage(messageEditData).submit()
    }

    /**
     * Invalidates the component callback
     *
     * If this component is invocated in the future, it will fail with "callback not found"!
     *
     * Useful if you are making a "one shot" component
     */
    fun invalidateComponentCallback() {
        val componentId = UnleashedComponentId(event.componentId)

        when (event.componentType) {
            Component.Type.UNKNOWN -> TODO()
            Component.Type.ACTION_ROW -> TODO()
            Component.Type.BUTTON -> {
                loritta.interactivityManager.buttonInteractionCallbacks.remove(componentId.uniqueId)
            }
            Component.Type.STRING_SELECT -> {
                loritta.interactivityManager.selectMenuInteractionCallbacks.remove(componentId.uniqueId)
            }
            Component.Type.TEXT_INPUT -> TODO()
            Component.Type.USER_SELECT -> TODO()
            Component.Type.ROLE_SELECT -> TODO()
            Component.Type.MENTIONABLE_SELECT -> TODO()
            Component.Type.CHANNEL_SELECT -> TODO()
            Component.Type.SECTION -> TODO()
            Component.Type.TEXT_DISPLAY -> TODO()
            Component.Type.THUMBNAIL -> TODO()
            Component.Type.MEDIA_GALLERY -> TODO()
            Component.Type.FILE_DISPLAY -> TODO()
            Component.Type.SEPARATOR -> TODO()
            Component.Type.CONTAINER -> TODO()
        }
    }

    suspend fun sendModal(
        title: String,
        components: List<ActionRow>,
        callback: suspend (ModalContext, ModalArguments) -> (Unit)
    ) {
        val unleashedComponentId = UnleashedComponentId(UUID.randomUUID())
        loritta.interactivityManager.modalCallbacks[unleashedComponentId.uniqueId] = InteractivityManager.ModalInteractionCallback(this.alwaysEphemeral, callback)

        event.replyModal(
            unleashedComponentId.toString(),
            title,
            components
        ).await()
    }
}