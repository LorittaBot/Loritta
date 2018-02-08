package com.mrpowergamerbr.loritta.userdata

class ModerationConfig {
	var sendPunishmentViaDm: Boolean = false
	var sendToPunishLog: Boolean = false
	var punishmentLogChannelId: String? = null
	var punishmentLogMessage: String = "**Usuário punido:** {user}#{#user}**Punido por** {@staff}\n**Motivo:** {reason}"
}