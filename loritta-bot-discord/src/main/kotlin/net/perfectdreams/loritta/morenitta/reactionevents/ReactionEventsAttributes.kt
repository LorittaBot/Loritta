package net.perfectdreams.loritta.morenitta.reactionevents

import net.perfectdreams.loritta.morenitta.reactionevents.events.Anniversary2025ReactionEvent
import net.perfectdreams.loritta.morenitta.reactionevents.events.Christmas2024ReactionEvent
import net.perfectdreams.loritta.morenitta.reactionevents.events.Halloween2024ReactionEvent
import net.perfectdreams.loritta.morenitta.reactionevents.events.Halloween2025ReactionEvent
import java.time.Instant

object ReactionEventsAttributes {
    // These are special attributes that can't be stored in a database due to one way or another (example: if they would feel bad)
    val attributes = mutableMapOf<String, ReactionEvent>()

    init {
        register(Halloween2024ReactionEvent)
        register(Christmas2024ReactionEvent)
        register(Anniversary2025ReactionEvent)
        register(Halloween2025ReactionEvent)
    }

    private fun register(event: ReactionEvent) {
        attributes[event.internalId] = event
    }

    /**
     * Gets the current active event or null if none are active
     */
    fun getActiveEvent(now: Instant): ReactionEvent? {
        return attributes.values.firstOrNull {
            now >= it.startsAt && it.endsAt > now
        }
    }
}