package net.perfectdreams.loritta.cinnamon.common.utils

data class LorittaPlaceholder(
    val name: String
) {
    val asKey: String
        get() = Placeholders.createPlaceholderKey(name)
}