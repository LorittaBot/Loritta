package net.perfectdreams.loritta.morenitta.interactions.components

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class ComponentManager {
    companion object {
        // "Interaction tokens are valid for 15 minutes, meaning you can respond to an interaction within that amount of time."
        // However technically we don't care about the token invalidation time if a component is clicked
        val INTERACTION_INVALIDATION_DELAY = 5.minutes
    }

    val scope = CoroutineScope(Dispatchers.Default)
    val buttonInteractionCallbacks = ConcurrentHashMap<UUID, suspend (ComponentContext) -> (Unit)>()

    /**
     * Creates an interactive button
     */
    fun button(style: ButtonStyle, label: String, callback: suspend (ComponentContext) -> (Unit)): Button {
        val buttonId = UUID.randomUUID()
        buttonInteractionCallbacks[buttonId] = callback
        return Button.of(style, buttonId.toString(), label)
    }
}