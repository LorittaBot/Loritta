package net.perfectdreams.loritta.plugin.funky.transformers

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.toJsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.website.utils.config.types.ConfigTransformer

object VoiceChannelsTransformer : ConfigTransformer {
	override val payloadType: String = "voicechannels"
	override val configKey: String = "voiceChannels"

	override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
		throw NotImplementedError()
	}

	override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
		return guild.voiceChannels.map {
			jsonObject(
					"id" to it.id,
					"name" to it.name
			)
		}.toJsonArray()
	}
}