package net.perfectdreams.loritta.common.utils.placeholders

@Deprecated("This should not be used, use the new placeholder system instead.")
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