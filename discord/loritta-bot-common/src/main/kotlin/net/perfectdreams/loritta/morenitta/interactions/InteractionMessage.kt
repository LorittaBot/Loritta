package net.perfectdreams.loritta.morenitta.interactions

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.perfectdreams.loritta.morenitta.utils.extensions.await

/**
 * An interaction message that supports initial interaction message (which does NOT have the message data) and follow-up messages (which DO have the message data)
 */
interface InteractionMessage {
    /**
     * Retrieves the original interaction message.
     *
     * **Does not work with ephemeral messages!**
     */
    suspend fun retrieveOriginal(): Message

    class InitialInteractionMessage(val hook: InteractionHook) : InteractionMessage {
        override suspend fun retrieveOriginal(): Message = hook.retrieveOriginal().await()
    }

    class FollowUpInteractionMessage(val message: Message) : InteractionMessage {
        override suspend fun retrieveOriginal() = message
    }
}