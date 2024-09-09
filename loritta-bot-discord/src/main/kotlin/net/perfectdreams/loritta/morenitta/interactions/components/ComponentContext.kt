package net.perfectdreams.loritta.morenitta.interactions.components

import dev.minn.jda.ktx.interactions.components.option
import dev.minn.jda.ktx.interactions.components.replyModal
import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import dev.minn.jda.ktx.messages.MessageEditBuilder
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.Component
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.interactions.commands.styled
import net.perfectdreams.loritta.cinnamon.discord.utils.LoadingEmojis
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext
import net.perfectdreams.loritta.morenitta.interactions.UnleashedComponentId
import net.perfectdreams.loritta.morenitta.interactions.UnleashedHook
import net.perfectdreams.loritta.morenitta.interactions.UnleashedMentions
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalArguments
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalContext
import net.perfectdreams.loritta.morenitta.utils.LorittaUser
import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.morenitta.utils.extensions.await
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
        hook: InteractionHook,
        updateMessageContent: Boolean = true,
        disableComponents: Boolean = true,
        loadingEmoji: DiscordEmote = LoadingEmojis.random()
    ) {
        val builtMessage = MessageEdit {
            if (updateMessageContent)
                styled(
                    i18nContext.get(I18nKeysData.Website.Dashboard.Loading),
                    loadingEmoji
                )

            if (disableComponents)
                disableComponents(loadingEmoji)
        }

        hook.editOriginal(builtMessage).await()
    }

    fun InlineMessage<*>.disableComponents(
        loadingEmoji: DiscordEmote
    ) {
        val buttonComponents = mutableListOf<ItemComponent>()
        val selectMenuComponents = mutableListOf<ItemComponent>()

        event.message.components.forEach { (it as Component)
            if (it.type == Component.Type.ACTION_ROW) {
                it.components.forEach { (it as Component)
                    when (it.type) {
                        Component.Type.BUTTON -> { (it as Button)
                            buttonComponents.add(loritta.interactivityManager.disabledButton(
                                it.style
                            ) {
                                emoji = if (event.componentId == it.id) {
                                    Emoji.fromCustom(
                                        loadingEmoji.name,
                                        loadingEmoji.id,
                                        loadingEmoji.animated
                                    )
                                } else {
                                    it.emoji!!.asCustom()
                                }
                            })
                        }

                        Component.Type.STRING_SELECT -> { (it as StringSelectMenu)
                            selectMenuComponents.add(loritta.interactivityManager.stringSelectMenu({
                                val minValues = if (it.minValues == 0) 1 else it.minValues
                                val maxValues = if (it.maxValues == 0) 1 else it.maxValues

                                this.maxValues = maxValues
                                this.minValues = minValues

                                isDisabled = true

                                if (placeholder != null)
                                    placeholder = it.placeholder

                                if (options.isEmpty()) {
                                    if (minValues == 1 && maxValues == 1) {
                                        option(
                                            i18nContext.get(
                                                I18nKeysData.Website.Dashboard.Loading
                                            ),
                                            "loading_psst_hey_u_are_cute_uwu",
                                            emoji = Emoji.fromCustom(
                                                loadingEmoji.name,
                                                loadingEmoji.id,
                                                loadingEmoji.animated
                                            ),
                                            default = true
                                        )
                                    } else {
                                        it.options.forEach {
                                            option(
                                                it.label,
                                                it.value,
                                                it.description,
                                                it.emoji,
                                                default = it.value in this.options.map { it?.value }
                                            )
                                        }
                                    }
                                }

                            }){ context, values ->
                                // Do nothing, the user cannot interact with this anyway :p
                            })
                        }

                        Component.Type.TEXT_INPUT -> error("This shouldn't exist here!")
                        Component.Type.UNKNOWN -> error("This shouldn't exist here!")
                        else -> error("Unknown component type ${it.type}")
                    }
                }
            }
        }

        if (buttonComponents.isNotEmpty())
            actionRow(buttonComponents)

        if (selectMenuComponents.isNotEmpty())
            actionRow(selectMenuComponents)
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
        }
    }

    suspend fun sendModal(
        title: String,
        components: List<LayoutComponent>,
        callback: suspend (ModalContext, ModalArguments) -> (Unit)
    ) {
        val unleashedComponentId = UnleashedComponentId(UUID.randomUUID())
        loritta.interactivityManager.modalCallbacks[unleashedComponentId.uniqueId] = callback

        event.replyModal(
            unleashedComponentId.toString(),
            title,
            components
        ).await()
    }
}