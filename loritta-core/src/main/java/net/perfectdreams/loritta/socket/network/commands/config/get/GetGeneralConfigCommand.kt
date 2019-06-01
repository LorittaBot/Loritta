package net.perfectdreams.loritta.socket.network.commands.config.get

import com.fasterxml.jackson.databind.node.ObjectNode
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import net.perfectdreams.loritta.socket.network.ConfigSectionOpCode
import net.perfectdreams.loritta.utils.extensions.objectNode

class GetGeneralConfigCommand : GetConfigCommand(ConfigSectionOpCode.GENERAL) {
    override suspend fun process(guildId: Long, serverConfig: MongoServerConfig): ObjectNode {
        val objectNode = objectNode()

        val fields = listOf(
                MongoServerConfig::commandPrefix,
                // MongoServerConfig::explainOnCommandRun,
                MongoServerConfig::mentionOnCommandOutput,
                MongoServerConfig::deleteMessageAfterCommand,
                MongoServerConfig::warnOnMissingPermission,
                MongoServerConfig::warnOnUnknownCommand,
                MongoServerConfig::warnIfBlacklisted,
                MongoServerConfig::blacklistWarning
        )

        fields.forEach { field ->
            objectNode.putPOJO(field.name, field.get(serverConfig))
        }

        return objectNode
    }
}