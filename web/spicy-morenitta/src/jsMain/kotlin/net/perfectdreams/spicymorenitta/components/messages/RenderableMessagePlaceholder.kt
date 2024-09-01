package net.perfectdreams.spicymorenitta.components.messages

import net.perfectdreams.loritta.common.utils.placeholders.MessagePlaceholder

/**
 * A Loritta's renderable message placeholder
 */
data class RenderableMessagePlaceholder(
    val placeholder: MessagePlaceholder,
    val replaceWith: String
)