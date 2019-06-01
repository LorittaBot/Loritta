package net.perfectdreams.loritta.socket.network.commands.config

import com.fasterxml.jackson.databind.JsonNode
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig

abstract class PatchConfigCommand(val op: Int) {
    abstract suspend fun process(serverConfig: MongoServerConfig, payload: JsonNode)
}