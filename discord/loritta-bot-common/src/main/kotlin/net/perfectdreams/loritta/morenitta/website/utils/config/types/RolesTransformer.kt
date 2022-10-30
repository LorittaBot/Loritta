package net.perfectdreams.loritta.morenitta.website.utils.config.types

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.deviousfun.entities.Guild

object RolesTransformer : ConfigTransformer {
    override val payloadType: String = "roles"
    override val configKey: String = "roles"

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        throw NotImplementedError()
    }

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        return guild.roles.map {
            jsonObject(
                "id" to it.id,
                "name" to it.name,
                "colorRaw" to it.colorRaw,
                "canInteract" to guild.retrieveSelfMember().canInteract(it),
                "isHoisted" to it.isHoisted,
                "isManaged" to it.isManaged,
                "isPublicRole" to it.isPublicRole
            )
        }.toJsonArray()
    }
}