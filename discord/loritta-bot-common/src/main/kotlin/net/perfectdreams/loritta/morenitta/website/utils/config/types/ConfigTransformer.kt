package net.perfectdreams.loritta.morenitta.website.utils.config.types

import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession

interface ConfigTransformer {
    val payloadType: String
    val configKey: String

    suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        throw NotImplementedError()
    }

    suspend fun fromJson(
        userIdentification: LorittaJsonWebSession.UserIdentification,
        guild: Guild,
        serverConfig: ServerConfig,
        payload: JsonObject
    ) = fromJson(guild, serverConfig, payload)

    suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        throw NotImplementedError()
    }

    suspend fun toJson(
        userIdentification: LorittaJsonWebSession.UserIdentification,
        guild: Guild,
        serverConfig: ServerConfig
    ): JsonElement = toJson(guild, serverConfig)
}