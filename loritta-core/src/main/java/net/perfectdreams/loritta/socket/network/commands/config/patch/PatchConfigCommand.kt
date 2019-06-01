package net.perfectdreams.loritta.socket.network.commands.config.patch

import com.fasterxml.jackson.databind.JsonNode
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig

abstract class PatchConfigCommand(val sectionName: String) {
    abstract suspend fun process(serverConfig: MongoServerConfig, payload: JsonNode)
}