package net.perfectdreams.loritta.common.utils.placeholders

import net.perfectdreams.i18nhelper.core.keydata.StringI18nData

/**
 * A Loritta's message placeholder
 */
@Deprecated("This should not be used, use the new placeholder system instead.")
interface MessagePlaceholder {
    /**
     * The placeholder names
     */
    val names: List<HidableLorittaPlaceholder>

    /**
     * The placeholder description
     */
    val description: StringI18nData?

    /**
     * The placeholder render type on the frontend
     */
    val renderType: RenderType

    enum class RenderType {
        TEXT,
        MENTION,
    }
}