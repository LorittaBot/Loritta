package net.perfectdreams.loritta.placeholders

import net.perfectdreams.loritta.common.utils.placeholders.HidableLorittaPlaceholder

// This should be removed after we migrate everything
fun HidableLorittaPlaceholder.toNewPlaceholderSystem() = LorittaPlaceholder(
    this.placeholder.name,
    this.hidden
)