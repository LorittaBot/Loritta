package com.mrpowergamerbr.loritta.website.views.subviews.api.config.types

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonObject
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import net.dv8tion.jda.api.entities.Guild
import net.perfectdreams.loritta.dao.servers.moduleconfigs.ModerationConfig
import net.perfectdreams.loritta.tables.servers.moduleconfigs.WarnActions
import net.perfectdreams.loritta.utils.PunishmentAction
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction

class ModerationPayload : ConfigPayloadType("moderation") {
	override fun process(payload: JsonObject, userIdentification: LorittaJsonWebSession.UserIdentification, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig, guild: Guild) {
		val sendPunishmentViaDm = payload["sendPunishmentViaDm"].bool
		val sendPunishmentToPunishLog = payload["sendToPunishLog"].bool
		val punishmentLogChannelId = payload["punishmentLogChannelId"].nullLong
		val punishmentLogMessage = payload["punishmentLogMessage"].nullString
		val punishmentActions = payload["punishmentActions"].array

		transaction(Databases.loritta) {
			val moderationConfig = serverConfig.moderationConfig

			if (moderationConfig != null) {
				WarnActions.deleteWhere {
					WarnActions.config eq moderationConfig.id
				}
			}

			val newConfig = moderationConfig ?: ModerationConfig.new {
				this.sendPunishmentViaDm = sendPunishmentViaDm
				this.sendPunishmentToPunishLog = sendPunishmentToPunishLog
				this.punishLogChannelId = punishmentLogChannelId
				this.punishLogMessage = punishmentLogMessage
			}

			newConfig.sendPunishmentViaDm = sendPunishmentViaDm
			newConfig.sendPunishmentToPunishLog = sendPunishmentToPunishLog
			newConfig.punishLogChannelId = punishmentLogChannelId
			newConfig.punishLogMessage = punishmentLogMessage

			serverConfig.moderationConfig = newConfig

			for (punishmentAction in punishmentActions.map { it.obj }) {
				val action = punishmentAction["punishmentAction"].string
				val warnCount = punishmentAction["warnCount"].int
				val time = punishmentAction["time"].nullString

				WarnActions.insert {
					it[WarnActions.config] = newConfig.id
					it[WarnActions.punishmentAction] = PunishmentAction.valueOf(action)
					it[WarnActions.warnCount] = warnCount
					if (time != null) {
						it[WarnActions.metadata] = jsonObject(
								"time" to time
						)
					}
				}
			}
		}
	}
}