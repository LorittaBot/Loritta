package net.perfectdreams.loritta.api.commands

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale

open class Command<T : CommandContext>(
		val labels: List<String>,
		val description: (BaseLocale) -> (String),
		val executor: (suspend T.() -> (Unit))
) {
	var cooldown = 2_500
	var executedCount = 0
	var hasCommandFeedback = true
	var sendTypingStatus = false
	var onlyOwner = false
	// var lorittaPermissions = listOf()

}