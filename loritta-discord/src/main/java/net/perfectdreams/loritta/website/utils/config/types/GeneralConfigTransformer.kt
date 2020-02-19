package net.perfectdreams.loritta.website.utils.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.transactions.transaction

object GeneralConfigTransformer : ConfigTransformer {
    override val payloadType: String = "general"
    override val configKey: String = "general"

    override suspend fun toJson(userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig): JsonElement {
        return jsonObject(
                "localeId" to serverConfig.localeId,
                "commandPrefix" to serverConfig.commandPrefix,
                "deleteMessageAfterCommand" to serverConfig.deleteMessageAfterCommand,
                "warnOnMissingPermission" to serverConfig.warnOnMissingPermission,
                "warnOnUnknownCommand" to serverConfig.warnOnUnknownCommand,
                "blacklistedChannels" to serverConfig.blacklistedChannels.toList().toJsonArray(),
                "warnIfBlacklisted" to serverConfig.warnIfBlacklisted,
                "blacklistedWarning" to serverConfig.blacklistedWarning
        )
    }

    override suspend fun fromJson(userIdentification: LorittaJsonWebSession.UserIdentification, guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        transaction(Databases.loritta) {
            serverConfig.commandPrefix = payload["commandPrefix"].string
            serverConfig.deleteMessageAfterCommand = payload["deleteMessageAfterCommand"].bool
            serverConfig.warnOnUnknownCommand = payload["warnOnUnknownCommand"].bool
            serverConfig.warnOnMissingPermission = payload["warnOnMissingPermission"].bool
            serverConfig.warnIfBlacklisted = payload["warnIfBlacklisted"].bool
            serverConfig.blacklistedChannels = payload["blacklistedChannels"].array.map { it.long }.toTypedArray()
            serverConfig.blacklistedWarning = payload["blacklistedWarning"].nullString
        }
    }
}