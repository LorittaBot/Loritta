package net.perfectdreams.loritta.parallax.executors

import net.perfectdreams.loritta.parallax.api.packet.ParallaxPacket
import net.perfectdreams.loritta.parallax.api.packet.ParallaxSerializer
import java.io.BufferedWriter

interface PacketExecutor {
    fun writeAckPacket(outputStream: BufferedWriter, ackId: Long, packet: ParallaxPacket) {
        outputStream.write(ParallaxSerializer.toJson(packet, ackId) + "\n")
        outputStream.flush()
    }
}