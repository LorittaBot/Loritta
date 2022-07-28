package net.perfectdreams.loritta.cinnamon.utils

data class LorittaPlaceholder(
    val name: String
) {
    val asKey: String
        get() = Placeholders.createPlaceholderKey(name)
}