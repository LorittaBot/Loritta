package net.perfectdreams.loritta.api.commands

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LocaleKeyData
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.platform.PlatformFeature

open class Command<T : CommandContext>(
		val loritta: LorittaBot,
		val labels: List<String>,
		val commandName: String,
		val category: CommandCategory,
		val descriptionKey: LocaleKeyData = MISSING_DESCRIPTION_KEY,
		val description: ((BaseLocale) -> (String)) = {
			it.get(descriptionKey)
		},
		val usage: CommandArguments,
		val examplesKey: LocaleKeyData?,
		val executor: (suspend T.() -> (Unit))
) {
	companion object {
		val MISSING_DESCRIPTION_KEY = LocaleKeyData("commands.missingDescription")
		val SINGLE_IMAGE_EXAMPLES_KEY = LocaleKeyData("commands.images.singleImageExamples")
		val TWO_IMAGES_EXAMPLES_KEY = LocaleKeyData("commands.images.twoImagesExamples")
	}

	var needsToUploadFiles = false
	var hideInHelp = false
	var hasCommandFeedback = true
	var sendTypingStatus = false
	var canUseInPrivateChannel = false
	var onlyOwner = false
	var requiredFeatures: List<PlatformFeature> = listOf()
	var similarCommands: List<String> = listOf()

	open val cooldown = 2_500
	// var lorittaPermissions = listOf()

}