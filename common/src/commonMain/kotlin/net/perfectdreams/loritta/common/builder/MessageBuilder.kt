package net.perfectdreams.loritta.common.builder

import net.perfectdreams.loritta.common.entities.LorittaEmbed
import net.perfectdreams.loritta.common.entities.LorittaMessage

class MessageBuilder {
    var content: String? = null
    var embed: LorittaEmbed? = null
    // There isn't a multiplatform input stream (sad)
    var files = mutableMapOf<String, ByteArray>()

    fun build() = LorittaMessage(
        content ?: " ",
        embed,
        files
    )

    fun addFile(fileName: String, stream: ByteArray) {
        files[fileName] = stream
    }
}