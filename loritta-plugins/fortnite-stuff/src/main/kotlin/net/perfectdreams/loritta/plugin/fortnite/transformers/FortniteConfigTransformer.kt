package net.perfectdreams.loritta.plugin.fortnite.transformers

import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.plugin.fortnite.dao.FortniteConfig
import net.perfectdreams.loritta.plugin.fortnite.tables.fortniteConfig
import net.perfectdreams.loritta.website.utils.config.types.ConfigTransformer
import org.jetbrains.exposed.sql.transactions.transaction

object FortniteConfigTransformer : ConfigTransformer {
	override val payloadType = "fortnite"
	override val configKey = "fortniteConfig"

	override suspend fun toJson(guild: Guild, serverConfig: ServerConfig): JsonElement {
		val fortniteConfig = transaction(Databases.loritta) { serverConfig.fortniteConfig }
		return jsonObject(
				"advertiseNewItems" to (fortniteConfig?.advertiseNewItems ?: false),
				"channelToAdvertiseNewItems" to fortniteConfig?.channelToAdvertiseNewItems
		)
	}

	override suspend fun fromJson(guild: Guild, serverConfig: ServerConfig, payload: JsonObject) {
		transaction(Databases.loritta) {
			val fortniteConfig = serverConfig.fortniteConfig ?: FortniteConfig.new {}

			fortniteConfig.advertiseNewItems = payload["advertiseNewItems"].bool
			if (fortniteConfig.advertiseNewItems)
				fortniteConfig.channelToAdvertiseNewItems = payload["channelToAdvertiseNewItems"].long
			else
				fortniteConfig.channelToAdvertiseNewItems = null
			serverConfig.fortniteConfig = fortniteConfig
		}
	}
}