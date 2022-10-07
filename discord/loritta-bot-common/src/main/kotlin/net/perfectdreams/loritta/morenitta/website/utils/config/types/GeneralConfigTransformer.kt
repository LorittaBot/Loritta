package net.perfectdreams.loritta.morenitta.website.utils.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.morenitta.LorittaBot

class GeneralConfigTransformer(val loritta: LorittaBot) : ConfigTransformer {
    override val payloadType: String = "general"
    override val configKey: String = "general"

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        return jsonObject(
                "localeId" to serverConfig.localeId,
                "commandPrefix" to serverConfig.commandPrefix,
                "deleteMessageAfterCommand" to serverConfig.deleteMessageAfterCommand,
                "warnOnUnknownCommand" to serverConfig.warnOnUnknownCommand,
                "blacklistedChannels" to serverConfig.blacklistedChannels.toList().toJsonArray(),
                "warnIfBlacklisted" to serverConfig.warnIfBlacklisted,
                "blacklistedWarning" to serverConfig.blacklistedWarning
        )
    }

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        loritta.newSuspendedTransaction {
            serverConfig.commandPrefix = payload["commandPrefix"].string
            serverConfig.deleteMessageAfterCommand = payload["deleteMessageAfterCommand"].bool
            serverConfig.warnOnUnknownCommand = payload["warnOnUnknownCommand"].bool
            serverConfig.warnIfBlacklisted = payload["warnIfBlacklisted"].bool
            serverConfig.blacklistedChannels = payload["blacklistedChannels"].array.map { it.long }.toTypedArray()
            serverConfig.blacklistedWarning = payload["blacklistedWarning"].nullString
        }
    }
}