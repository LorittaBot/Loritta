package net.perfectdreams.loritta.socket.network.commands.config.get

import com.fasterxml.jackson.databind.node.ObjectNode
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig

abstract class GetConfigCommand(val sectionName: String) {
    abstract suspend fun process(guildId: Long, serverConfig: MongoServerConfig): ObjectNode
}