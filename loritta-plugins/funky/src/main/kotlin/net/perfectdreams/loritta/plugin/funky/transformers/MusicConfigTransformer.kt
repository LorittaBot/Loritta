package net.perfectdreams.loritta.plugin.funky.transformers

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.plugin.funky.dao.MusicConfig
import net.perfectdreams.loritta.plugin.funky.tables.musicConfig
import net.perfectdreams.loritta.website.utils.config.types.ConfigTransformer
import org.jetbrains.exposed.sql.transactions.transaction

object MusicConfigTransformer : ConfigTransformer {
	override val payloadType = "music"
	override val configKey = "musicConfig"

	override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
		val musicConfig = transaction(Databases.loritta) { serverConfig.musicConfig }
		return jsonObject(
				"enabled" to (musicConfig?.enabled ?: false),
				"channels" to (musicConfig?.channels ?: arrayOf()).toList().toJsonArray()
		)
	}

	override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
		transaction(Databases.loritta) {
			val musicConfig = serverConfig.musicConfig ?: MusicConfig.new {
				this.enabled = false
				this.channels = arrayOf()
			}

			musicConfig.enabled = payload["enabled"].bool
			musicConfig.channels = payload["channels"].array.map { it.long }.toTypedArray()
			serverConfig.musicConfig = musicConfig
		}
	}
}