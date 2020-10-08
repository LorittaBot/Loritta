package net.perfectdreams.loritta.parallax.executors

import net.perfectdreams.loritta.parallax.api.packet.ParallaxAckPacket
import net.perfectdreams.loritta.parallax.api.packet.ParallaxLogPacket
import net.perfectdreams.loritta.parallax.api.packet.ParallaxPacket
import java.io.BufferedWriter

object LogExecutor : PacketExecutor {
    suspend fun executes(ackId: Long, data: ParallaxLogPacket, guildId: Long, channelId: Long, clusterUrl: String, outputStream: BufferedWriter) {
        println("Log: ${data.message}")
        writeAckPacket(outputStream, ackId, ParallaxAckPacket())
    }
}