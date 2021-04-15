package net.perfectdreams.loritta.common.builder

import net.perfectdreams.loritta.common.entities.LorittaEmbed
import net.perfectdreams.loritta.common.entities.LorittaMessage

class MessageBuilder {
    var content: String? = null
    var embed: LorittaEmbed? = null

    fun build() = LorittaMessage(
        content ?: " ",
        embed
    )
}