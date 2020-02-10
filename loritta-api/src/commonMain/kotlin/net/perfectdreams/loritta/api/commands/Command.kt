package net.perfectdreams.loritta.api.commands

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.platform.PlatformFeature

open class Command<T : CommandContext>(
		val loritta: LorittaBot,
		val labels: List<String>,
		val commandName: String,
		val description: ((BaseLocale) -> (String))?,
		val usage: CommandArguments,
		val examples: ((BaseLocale) -> (List<String>))? = null,
		val executor: (suspend T.() -> (Unit))
) {
	var needsToUploadFiles = false
	var hideInHelp = false
	var hasCommandFeedback = true
	var sendTypingStatus = false
	var canUseInPrivateChannel = false
	var onlyOwner = false
	var requiredFeatures: List<PlatformFeature> = listOf()

	open val cooldown = 2_500
	var executedCount = 0
	// var lorittaPermissions = listOf()

}