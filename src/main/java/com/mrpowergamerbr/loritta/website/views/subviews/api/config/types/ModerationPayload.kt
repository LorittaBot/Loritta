package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.bool
import com.github.salomonbrys.kotson.fromJson
import com.github.salomonbrys.kotson.string
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.oauth2.TemmieDiscordAuth
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.entities.Guild

class ModerationPayload : ConfigPayloadType("moderation") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun process(payload: JsonObject, userIdentification: TemmieDiscordAuth.UserIdentification, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, guild: Guild) {
		val moderationConfig = legacyServerConfig.moderationConfig
		moderationConfig.sendPunishmentViaDm = payload["sendPunishmentViaDm"].bool
		moderationConfig.sendToPunishLog = payload["sendToPunishLog"].bool
		if (!moderationConfig.useLorittaBansNetwork && payload["useLorittaBansNetwork"].bool) {
			GlobalScope.launch(loritta.coroutineDispatcher) {
				for (entry in loritta.networkBanManager.networkBannedUsers) {
					try {
						val user = lorittaShards.getUserById(entry.id) ?: continue

						loritta.networkBanManager.punishUser(user, loritta.networkBanManager.createBanReason(entry, true), guild)
					} catch (e: Exception) {
						logger.error(e) { "Erro ao processar entry de ${entry.id}!" }
					}
				}
			}
		}
		moderationConfig.useLorittaBansNetwork = payload["useLorittaBansNetwork"].bool
		moderationConfig.punishmentLogChannelId = payload["punishmentLogChannelId"].string
		moderationConfig.punishmentLogMessage = payload["punishmentLogMessage"].string

		moderationConfig.punishmentActions = Gson().fromJson(payload["punishmentActions"])
	}
}