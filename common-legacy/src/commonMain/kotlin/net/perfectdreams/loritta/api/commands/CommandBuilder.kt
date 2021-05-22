package net.perfectdreams.loritta.api.commands

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.locale.LocaleStringData

fun Any?.command(
	loritta: LorittaBot,
	labels: List<String>,
	category: CommandCategory,
	builder: CommandBuilder<CommandContext>.() -> (Unit)
) = command(loritta, this?.let { this::class.simpleName } ?: "UnknownCommand", labels, category, builder)

fun command(loritta: LorittaBot, commandName: String, labels: List<String>, category: CommandCategory, builder: CommandBuilder<CommandContext>.() -> (Unit)): Command<CommandContext> {
	val b = CommandBuilder<CommandContext>(loritta, commandName, labels, category)
	builder.invoke(b)
	return b.build()
}

open class CommandBuilder<context : CommandContext>(
		// Needs to be private to avoid accessing this variable on the builder itself
		private val loritta: LorittaBot,
		val commandName: String,
		val labels: List<String>,
		val category: CommandCategory
) {
	var canUseInPrivateChannel = false
	var needsToUploadFiles = false
	var hideInHelp = false
	var onlyOwner = false
	var similarCommands = listOf<String>()
	var sendTypingStatus = false

	// I don't really like the name of this variable, the reason we purposely prefix this with "builder" is to avoid acessing
	// "descriptionKey" inside of a command builder block, causing issues.
	// (Example: You want to access a variable named "descriptionKey" within your builder block... it would get the "MISSING_DESCRIPTION_KEY" key!)
	//
	// And yes, this can't be "private" because then classes extending the builder can't access the description key
	var builderDescriptionKey = Command.MISSING_DESCRIPTION_KEY
	var builderExamplesKey: LocaleKeyData? = null

	var descriptionCallback: ((BaseLocale) -> (String))? = null
	var usageCallback: (CommandArgumentsBuilder.() -> Unit)? = null
	var examplesCallback: ((BaseLocale) -> (List<String>))? = null
	var executeCallback: (suspend context.() -> (Unit))? = null

	/**
	 * Gets the description from the specified [localeKey] with the [arguments] from the [BaseLocale]
	 *
	 * This does not use the [descriptionCallback]!
	 *
	 * This is a helper method for the [localizedDescription] method
	 *
	 * @see BaseLocale
	 * @see descriptionKey
	 */
	fun localizedDescription(localeKey: String, vararg arguments: Any?)
			= localizedDescription(LocaleKeyData(localeKey, arguments.map { LocaleStringData(it.toString()) }))

	/**
	 * Gets the description from the specified [localeKey] with the [arguments] from the [BaseLocale]
	 *
	 * This does not use the [descriptionCallback]!
	 *
	 * @see BaseLocale
	 * @see descriptionKey
	 */
	fun localizedDescription(localeKey: LocaleKeyData) {
		builderDescriptionKey = localeKey
	}

	fun description(callback: (BaseLocale) -> (String)) {
		this.descriptionCallback = callback
	}

	fun usage(callback: CommandArgumentsBuilder.() -> Unit) {
		this.usageCallback = callback
	}

	/**
	 * Gets the examples from the specified [localeKey] with the [arguments] from the [BaseLocale]
	 *
	 * This is a helper method for the [examples] method
	 *
	 * @see BaseLocale
	 * @see description
	 */
	fun localizedExamples(localeKey: String, vararg arguments: Any?)
			= localizedExamples(LocaleKeyData(localeKey, arguments.map { LocaleStringData(it.toString()) }))

	fun localizedExamples(localeKey: LocaleKeyData) {
		builderExamplesKey = localeKey
	}

	fun executes(callback: suspend context.() -> (Unit)) {
		this.executeCallback = callback
	}

	fun build(): Command<context> {
		val usage = arguments {
			usageCallback?.invoke(this)
		}

		return Command(
				loritta = loritta,
				labels = labels,
				commandName = commandName,
				category = category,
				descriptionKey = builderDescriptionKey,
				description = descriptionCallback ?: {
					it.get(builderDescriptionKey)
				},
				usage = usage,
				examplesKey = builderExamplesKey,
				executor = executeCallback!!
		).apply { build2().invoke(this) }
	}

	fun build2(): Command<context>.() -> (Unit) {
		return {
			this.canUseInPrivateChannel = this@CommandBuilder.canUseInPrivateChannel
			this.needsToUploadFiles = this@CommandBuilder.needsToUploadFiles
			this.hideInHelp = this@CommandBuilder.hideInHelp
			this.onlyOwner = this@CommandBuilder.onlyOwner
			this.similarCommands = this@CommandBuilder.similarCommands
			this.sendTypingStatus = this@CommandBuilder.sendTypingStatus
		}
	}
}