package net.perfectdreams.loritta.api.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData

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
		val SINGLE_IMAGE_EXAMPLES_KEY = LocaleKeyData("commands.category.images.singleImageExamples")
		val TWO_IMAGES_EXAMPLES_KEY = LocaleKeyData("commands.category.images.twoImagesExamples")
	}

	var needsToUploadFiles = false
	var hideInHelp = false
	var hasCommandFeedback = true
	var sendTypingStatus = false
	var canUseInPrivateChannel = false
	var onlyOwner = false
	var similarCommands: List<String> = listOf()

	open val cooldown = 2_500
	// var lorittaPermissions = listOf()

}