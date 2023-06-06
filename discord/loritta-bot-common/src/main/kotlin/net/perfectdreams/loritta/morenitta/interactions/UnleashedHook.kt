package net.perfectdreams.loritta.morenitta.interactions

import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.perfectdreams.loritta.morenitta.utils.extensions.await

abstract class UnleashedHook {
    class InteractionHook(val jdaHook: net.dv8tion.jda.api.interactions.InteractionHook) : UnleashedHook() {
        suspend fun editOriginal(message: MessageEditData): Message {
            return jdaHook.editOriginal(message).await()
        }
    }

    class LegacyMessageHook : UnleashedHook()
}