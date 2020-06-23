package net.perfectdreams.loritta.parallax.executors

import io.ktor.client.request.delete
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.readText
import io.ktor.http.userAgent
import net.perfectdreams.loritta.parallax.ParallaxServer
import net.perfectdreams.loritta.parallax.api.packet.ParallaxAckPacket
import net.perfectdreams.loritta.parallax.api.packet.ParallaxDeleteRolePacket
import java.io.BufferedWriter

object DeleteRoleExecutor : PacketExecutor {
    suspend fun executes(ackId: Long, data: ParallaxDeleteRolePacket, guildId: Long, channelId: Long, clusterUrl: String, outputStream: BufferedWriter) {
        println("Giving role ${data.roleId} to ${data.userId}")

        val response = ParallaxServer.http.delete<HttpResponse>("$clusterUrl/api/v1/parallax/guilds/$guildId/members/${data.userId}/roles/${data.roleId}") {
            this.userAgent(ParallaxServer.USER_AGENT)
            this.header("Authorization", ParallaxServer.authKey)
        }

        println(response.readText())

        writeAckPacket(outputStream, ackId, ParallaxAckPacket())
    }
}