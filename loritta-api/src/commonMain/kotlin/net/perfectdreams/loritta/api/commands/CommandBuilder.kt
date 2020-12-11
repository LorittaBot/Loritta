package net.perfectdreams.loritta.api.commands

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.platform.PlatformFeature

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
	var requiredFeatures = listOf<PlatformFeature>()
	var onlyOwner = false
	var similarCommands = listOf<String>()

	var descriptionCallback: ((BaseLocale) -> (String))? = null
	var usageCallback: (CommandArgumentsBuilder.() -> Unit)? = null
	var examplesCallback: ((BaseLocale) -> (List<String>))? = null
	var executeCallback: (suspend context.() -> (Unit))? = null

	/**
	 * Gets the description from the specified [localeKey] with the [arguments] from the [BaseLocale]
	 *
	 * This is a helper method for the [description] method
	 *
	 * @see BaseLocale
	 * @see description
	 */
	fun localizedDescription(localeKey: String, vararg arguments: Any?) = description { it.get(localeKey, *arguments) }

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
	fun localizedExamples(localeKey: String, vararg arguments: Any?) = examples {
		examples.addAll(it.getList(localeKey, arguments))
	}

	fun examples(callback: ExamplesWrapper.(BaseLocale) -> (Unit)) {
		this.examplesCallback = {
			val examplesWrapper = ExamplesWrapper()
			callback.invoke(examplesWrapper, it)
			examplesWrapper.examples
		}
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
				description = descriptionCallback ?: { "???" },
				usage = usage,
				examples = examplesCallback,
				executor = executeCallback!!
		).apply { build2().invoke(this) }
	}

	fun build2(): Command<context>.() -> (Unit) {
		return {
			this.canUseInPrivateChannel = this@CommandBuilder.canUseInPrivateChannel
			this.needsToUploadFiles = this@CommandBuilder.needsToUploadFiles
			this.hideInHelp = this@CommandBuilder.hideInHelp
			this.requiredFeatures = this@CommandBuilder.requiredFeatures
			this.onlyOwner = this@CommandBuilder.onlyOwner
			this.similarCommands = this@CommandBuilder.similarCommands
		}
	}

	class ExamplesWrapper {
		internal val examples = mutableListOf<String>()

		operator fun String.unaryPlus() = examples.add(this)
	}
}