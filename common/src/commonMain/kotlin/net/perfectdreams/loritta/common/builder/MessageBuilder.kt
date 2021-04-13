package net.perfectdreams.loritta.common.builder

import net.perfectdreams.loritta.common.entities.LorittaMessage

class MessageBuilder {
    var content: String? = null

    fun build() = LorittaMessage(
        content ?: " "
    )
}