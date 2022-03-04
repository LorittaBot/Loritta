package net.perfectdreams.loritta.cinnamon.platform.modals.components

sealed class ModalComponent<T>(
    val name: String,
)

// ===[ STRING ]===
class StringModalComponent(
    name: String
) : ModalComponent<String>(name)