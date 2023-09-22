package net.perfectdreams.loritta.common.utils.placeholders

data class LorittaPlaceholder(
    val name: String
) {
    val asKey: String
        get() = Placeholders.createPlaceholderKey(name)

    /**
     * Converts this placeholder into a [HidableLorittaPlaceholder] set to hidden
     */
    fun toHiddenPlaceholder() = HidableLorittaPlaceholder(this, true)

    /**
     * Converts this placeholder into a [HidableLorittaPlaceholder] set to not-hidden
     */
    fun toVisiblePlaceholder() = HidableLorittaPlaceholder(this, false)
}