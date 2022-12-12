package net.perfectdreams.loritta.morenitta.interactions.components

import kotlinx.coroutines.*
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.interactions.commands.InteraKTionsUnleashedDsl
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalArguments
import net.perfectdreams.loritta.morenitta.interactions.modals.ModalContext
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

// TODO: Rename this to "InteractivityManager"
class ComponentManager {
    companion object {
        // "Interaction tokens are valid for 15 minutes, meaning you can respond to an interaction within that amount of time."
        // However technically we don't care about the token invalidation time if a component is clicked
        val INTERACTION_INVALIDATION_DELAY = 5.minutes
    }

    val scope = CoroutineScope(Dispatchers.Default)
    var modalCallback: (suspend (ModalContext, ModalArguments) -> Unit)? = null
    val buttonInteractionCallbacks = ConcurrentHashMap<UUID, suspend (ComponentContext) -> (Unit)>()
    val pendingInteractionRemovals = ConcurrentLinkedQueue<suspend CoroutineScope.() -> (Unit)>()

    /**
     * Creates an interactive button
     */
    fun button(style: ButtonStyle, label: String, callback: suspend (ComponentContext) -> (Unit)): Button {
        val buttonId = UUID.randomUUID()
        buttonInteractionCallbacks[buttonId] = callback
        return Button.of(style, buttonId.toString(), label)
    }

    fun launch(block: suspend CoroutineScope.() -> Unit) = scope.launch(
        Dispatchers.Default,
        block = {
            pendingInteractionRemovals.add(block)

            delay(INTERACTION_INVALIDATION_DELAY)

            pendingInteractionRemovals.remove(block)

            block.invoke(this)
        }
    )
}