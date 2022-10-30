package net.perfectdreams.loritta.cinnamon.discord.interactions

import net.perfectdreams.discordinteraktions.common.builder.message.modify.InteractionOrFollowupMessageModifyBuilder
import net.perfectdreams.discordinteraktions.common.entities.messages.EditableMessage
import net.perfectdreams.loritta.cinnamon.discord.interactions.components.ComponentContext

/**
 * A "high level" (as in: above Discord InteraKTions' abstractions) editable message
 */
interface HighLevelEditableMessage {
    suspend fun editMessage(message: InteractionOrFollowupMessageModifyBuilder): EditableMessage
}

class ComponentContextHighLevelEditableMessage(private val context: ComponentContext) : HighLevelEditableMessage {
    override suspend fun editMessage(message: InteractionOrFollowupMessageModifyBuilder) =
        context.interaKTionsContext.updateMessage(message)
}

class SlashContextHighLevelEditableMessage(private val originalMessage: EditableMessage) : HighLevelEditableMessage {
    override suspend fun editMessage(message: InteractionOrFollowupMessageModifyBuilder) =
        originalMessage.editMessage(message)
}

// This isn't in the interface because we want this to be inline, allowing users to use suspendable functions within the builder
suspend inline fun HighLevelEditableMessage.editMessage(block: InteractionOrFollowupMessageModifyBuilder.() -> (Unit)) =
    editMessage(InteractionOrFollowupMessageModifyBuilder().apply(block))