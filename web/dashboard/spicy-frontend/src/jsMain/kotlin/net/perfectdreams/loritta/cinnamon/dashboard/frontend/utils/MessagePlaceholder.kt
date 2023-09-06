package net.perfectdreams.loritta.cinnamon.dashboard.frontend.utils

import net.perfectdreams.loritta.common.utils.LorittaPlaceholder

/**
 * A Loritta's message placeholder
 */
data class MessagePlaceholder(
    val name: String,
    val replaceWith: String,
    val description: String? = null,
    val renderType: RenderType,
    val hidden: Boolean
) {
    companion object {
        /**
         * Creates a [MessagePlaceholder] from a [LorittaPlaceholder]
         */
        operator fun invoke(
            placeholder: LorittaPlaceholder,
            replaceWith: String,
            description: String? = null,
            renderType: RenderType,
            hidden: Boolean
        ) = MessagePlaceholder(
            placeholder.name,
            replaceWith,
            description,
            renderType,
            hidden
        )
    }

    enum class RenderType {
        TEXT,
        MENTION
    }
}