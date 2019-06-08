package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import net.perfectdreams.loritta.socket.SocketWrapper
import net.perfectdreams.loritta.socket.network.SocketOpCode

class HeartbeatCommand : SocketCommand(SocketOpCode.HEARTBEAT) {
    override suspend fun process(socketWrapper: SocketWrapper, payload: JsonNode): JsonNode {
        println("Heartbeat payload is: " + payload)
        // val heartbeat = payload.obj["heartbeat"].textValue()

        return JsonNodeFactory.instance.objectNode()
                .put("heartbeat", System.currentTimeMillis())
    }
}