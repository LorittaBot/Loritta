package net.perfectdreams.loritta.socket.network.commands

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.JsonNodeFactory
import net.perfectdreams.loritta.socket.network.SocketOpCode

class UpdateStatusCommand : SocketCommand(SocketOpCode.UPDATE_STATUS_COUNT) {
    override suspend fun process(payload: JsonNode): JsonNode {
        return JsonNodeFactory.instance.objectNode()
    }
}