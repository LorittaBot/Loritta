package net.perfectdreams.loritta.common.utils

data class LorittaPlaceholder(
    val name: String
) {
    val asKey: String
        get() = Placeholders.createPlaceholderKey(name)
}