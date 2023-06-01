package net.perfectdreams.loritta.morenitta.interactions

import dev.minn.jda.ktx.messages.InlineMessage
import dev.minn.jda.ktx.messages.MessageEdit
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.interactions.InteractionHook
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
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

    suspend fun editMessage(builder: suspend InlineMessage<MessageEditData>.() -> (Unit)): Message

    class InitialInteractionMessage(val hook: InteractionHook) : InteractionMessage {
        override suspend fun retrieveOriginal(): Message = hook.retrieveOriginal().await()

        override suspend fun editMessage(builder: suspend InlineMessage<MessageEditData>.() -> Unit): Message = hook.editOriginal(
            MessageEdit {
                builder()
            }
        ).await()
    }

    class FollowUpInteractionMessage(val message: Message) : InteractionMessage {
        override suspend fun retrieveOriginal() = message

        override suspend fun editMessage(builder: suspend InlineMessage<MessageEditData>.() -> Unit): Message = message.editMessage(
            MessageEdit {
                builder()
            }
        ).await()
    }
}