package net.perfectdreams.loritta.parallax.api

import net.perfectdreams.loritta.parallax.api.packet.ParallaxAckSendMessagePacket
import net.perfectdreams.loritta.parallax.api.packet.ParallaxConnectionUtils
import net.perfectdreams.loritta.parallax.api.packet.ParallaxSendMessagePacket

class ParallaxMessageChannel(val idLong: Long) {
    val id: String
        get() = idLong.toString()

    fun sendMessage(message: String): ParallaxAckSendMessagePacket {
        val sendMessage = ParallaxSendMessagePacket(
                message
        )

        return ParallaxConnectionUtils.sendPacket(sendMessage)
    }
}