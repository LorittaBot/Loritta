package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import net.dv8tion.jda.core.entities.Guild

class ModerationPayload : ConfigPayloadType("moderation") {
	override fun process(payload: JsonObject, serverConfig: ServerConfig, guild: Guild) {
		val moderationConfig = serverConfig.moderationConfig
		moderationConfig.sendPunishmentViaDm = payload["sendPunishmentViaDm"].bool
		moderationConfig.sendToPunishLog = payload["sendToPunishLog"].bool
		moderationConfig.punishmentLogChannelId = payload["punishmentLogChannelId"].string
		moderationConfig.punishmentLogMessage = payload["punishmentLogMessage"].string

		moderationConfig.punishmentActions = Gson().fromJson(payload["punishmentActions"])
	}
}