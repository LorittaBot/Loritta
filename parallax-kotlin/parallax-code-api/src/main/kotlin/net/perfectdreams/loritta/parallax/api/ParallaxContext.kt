package net.perfectdreams.loritta.parallax.api

import net.perfectdreams.loritta.parallax.api.packet.ParallaxAckSendMessagePacket
import net.perfectdreams.loritta.parallax.api.packet.ParallaxConnectionUtils
import net.perfectdreams.loritta.parallax.api.packet.ParallaxSendMessagePacket
import net.perfectdreams.loritta.parallax.api.packet.ParallaxSerializer

class ParallaxContext {
    fun send(message: String): ParallaxAckSendMessagePacket {
        val sendMessage = ParallaxSendMessagePacket(
                message
        )

        return ParallaxConnectionUtils.sendPacket(sendMessage)
    }
}