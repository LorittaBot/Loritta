package userdata

class ModerationConfig {
	var sendPunishmentViaDm: Boolean = false
	var sendToPunishLog: Boolean = false
	var punishmentLogChannelId: String? = null
	var punishmentLogMessage: String = "**Usuário punido:** {user}#{#user}**Punido por** {@staff}\n**Motivo:** {reason}"
	var punishmentActions = arrayOf<WarnAction>()
	var warnExpiresIn: Long? = null

	class WarnAction constructor(
			var warnCount: Int,
			var punishmentAction: PunishmentAction,
			var customMetadata0: String?
	)

	class Warn constructor(
			var reason: String,
			var time: Long,
			var punishedBy: String
	)

	enum class PunishmentAction {
		BAN,
		SOFT_BAN,
		KICK,
		MUTE
	}
}