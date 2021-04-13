package net.perfectdreams.loritta.common.builder

import net.perfectdreams.loritta.common.entities.LorittaMessage

// Content is always required, that's why it is a constructor parameter
class MessageBuilder {
    var content: String? = null

    fun build() = LorittaMessage(
        content ?: " "
    )
}