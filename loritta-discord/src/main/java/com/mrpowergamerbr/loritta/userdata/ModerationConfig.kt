package com.mrpowergamerbr.loritta.userdata

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty

class ModerationConfig {
	@AllowReflection
	var sendPunishmentViaDm: Boolean = false
	@AllowReflection
	var sendToPunishLog: Boolean = false
	@AllowReflection
	var punishmentLogChannelId: String? = null
	@AllowReflection
	var punishmentLogMessage: String = "**Usuário punido:** {user}#{user-discriminator}\n**Punido por** {@staff}\n**Motivo:** {reason}"
	@AllowReflection
	var punishmentActions = mutableListOf<WarnAction>()
	@AllowReflection
	var warnExpiresIn: Long? = null
	@AllowReflection
	var useLorittaBansNetwork = false

	class WarnAction @BsonCreator constructor(
			@BsonProperty("warnCount")
			var warnCount: Int,
			@BsonProperty("punishmentAction")
			var punishmentAction: PunishmentAction
	) {
		var customMetadata0: String? = null // usado para mute "30 minutes"
		var customMetadata1: Int = 0 // usado para punições que permitem deletar os dias (como ban, softban)
	}

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
		KICK,
		MUTE
	}
}