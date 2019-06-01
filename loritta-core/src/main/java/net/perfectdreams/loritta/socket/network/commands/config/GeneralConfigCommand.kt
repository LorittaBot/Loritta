package net.perfectdreams.loritta.socket.network.commands.config

import com.fasterxml.jackson.databind.JsonNode
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import net.perfectdreams.loritta.socket.network.ConfigSectionOpCode

class GeneralConfigCommand : PatchConfigCommand(ConfigSectionOpCode.GENERAL) {
    override suspend fun process(serverConfig: MongoServerConfig, payload: JsonNode) {
        serverConfig.commandPrefix = payload["commandPrefix"].textValue()

        loritta save serverConfig
    }
}