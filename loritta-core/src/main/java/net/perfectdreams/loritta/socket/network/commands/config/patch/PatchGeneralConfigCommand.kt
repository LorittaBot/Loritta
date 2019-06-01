package net.perfectdreams.loritta.socket.network.commands.config.patch

import com.fasterxml.jackson.databind.JsonNode
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save

class PatchGeneralConfigCommand : PatchConfigCommand("general") {
    override suspend fun process(serverConfig: MongoServerConfig, payload: JsonNode) {
        serverConfig.commandPrefix = payload[MongoServerConfig::commandPrefix.name].textValue()
        serverConfig.mentionOnCommandOutput = payload[MongoServerConfig::mentionOnCommandOutput.name].booleanValue()
        serverConfig.deleteMessageAfterCommand = payload[MongoServerConfig::deleteMessageAfterCommand.name].booleanValue()
        serverConfig.warnOnMissingPermission = payload[MongoServerConfig::warnOnMissingPermission.name].booleanValue()
        serverConfig.warnOnUnknownCommand = payload[MongoServerConfig::warnOnUnknownCommand.name].booleanValue()
        serverConfig.warnIfBlacklisted = payload[MongoServerConfig::warnIfBlacklisted.name].booleanValue()
        serverConfig.blacklistWarning = payload[MongoServerConfig::blacklistWarning.name].textValue()

        loritta save serverConfig
    }
}