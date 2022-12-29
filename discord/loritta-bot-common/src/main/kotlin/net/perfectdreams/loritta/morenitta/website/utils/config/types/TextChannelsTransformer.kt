package net.perfectdreams.loritta.morenitta.website.utils.config.types

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.middleman.StandardGuildMessageChannel

object TextChannelsTransformer : ConfigTransformer {
    override val payloadType: String = "textchannels"
    override val configKey: String = "textChannels"

    override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
        throw NotImplementedError()
    }

    override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
        return guild.channels
            .filterIsInstance<StandardGuildMessageChannel>()
            .map {
                jsonObject(
                    "id" to it.idLong,
                    "canTalk" to it.canTalk(),
                    "name" to it.name,
                    "topic" to it.topic
                )
            }.toJsonArray()
    }
}