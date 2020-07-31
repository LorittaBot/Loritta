package net.perfectdreams.loritta.parallax.executors

import com.github.salomonbrys.kotson.jsonObject
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.userAgent
import net.perfectdreams.loritta.parallax.ParallaxServer
import net.perfectdreams.loritta.parallax.api.packet.ParallaxAckSendMessagePacket
import net.perfectdreams.loritta.parallax.api.packet.ParallaxSendMessagePacket
import java.io.BufferedWriter

object SendMessageExecutor : PacketExecutor {
    suspend fun executes(ackId: Long, data: ParallaxSendMessagePacket, guildId: Long, channelId: Long, clusterUrl: String, outputStream: BufferedWriter) {
        println("Sending message: ${data.content}")

        val response = ParallaxServer.http.post<HttpResponse>("$clusterUrl/api/v1/parallax/channels/$channelId/messages") {
            this.userAgent(ParallaxServer.USER_AGENT)
            this.header("Authorization", ParallaxServer.authKey)

            this.body = jsonObject(
                    "content" to data.content
            ).toString()
        }

        writeAckPacket(outputStream, ackId, ParallaxAckSendMessagePacket("successfully received :3"))
    }
}