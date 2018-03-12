package com.mrpowergamerbr.loritta.userdata

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

class ModerationConfig {
	var sendPunishmentViaDm: Boolean = false
	var sendToPunishLog: Boolean = false
	var punishmentLogChannelId: String? = null
	var punishmentLogMessage: String = "**Usuário punido:** {user}#{#user}**Punido por** {@staff}\n**Motivo:** {reason}"
	var punishmentActions = mutableListOf<WarnAction>()
	var warnExpiresIn: Long? = null

	class WarnAction @BsonCreator constructor(
			@BsonProperty("warnCount")
			var warnCount: Int,
			@BsonProperty("punishmentAction")
			var punishmentAction: PunishmentAction
	)

	class Warn @BsonCreator constructor(
			@BsonProperty("reason")
			var reason: String,
			@BsonProperty("time")
			var time: Long,
			@BsonProperty("punishedBy")
			var punishedBy: String
	)

	enum class PunishmentAction {
		BAN,
		SOFT_BAN,
		KICK
	}
}