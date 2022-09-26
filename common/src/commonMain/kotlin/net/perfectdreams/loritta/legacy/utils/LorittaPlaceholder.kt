package net.perfectdreams.loritta.legacy.utils

data class LorittaPlaceholder(
        val name: String
) {
    val asKey: String
        get() = Placeholders.createPlaceholderKey(name)
}