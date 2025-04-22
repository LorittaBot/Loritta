package net.perfectdreams.loritta.morenitta.interactions.components

import dev.minn.jda.ktx.coroutines.await
import dev.minn.jda.ktx.interactions.components.replyModal
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.interactions.components.ComponentInteraction
import net.perfectdreams.loritta.helper.LorittaHelper
import net.perfectdreams.loritta.morenitta.interactions.InteractionContext
import net.perfectdreams.loritta.morenitta.interactions.UnleashedComponentId
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalArguments
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalContext
import java.util.*

/**
 * Context of the executed command
 */
class ComponentContext(
    loritta: LorittaHelper,
    override val event: ComponentInteraction
) : InteractionContext(loritta) {
    suspend fun deferEdit(): InteractionHook = event.deferEdit().await()

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
        loritta.interactivityManager.modalCallbacks[unleashedComponentId.uniqueId] = callback

        event.replyModal(
            unleashedComponentId.toString(),
            title,
            components
        ).await()
    }
}