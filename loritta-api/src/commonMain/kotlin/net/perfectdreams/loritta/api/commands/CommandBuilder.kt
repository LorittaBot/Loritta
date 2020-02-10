package net.perfectdreams.loritta.api.commands

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.platform.PlatformFeature

fun command(loritta: LorittaBot, commandName: String, labels: List<String>, builder: CommandBuilder<CommandContext>.() -> (Unit)): Command<CommandContext> {
	val b = CommandBuilder<CommandContext>(loritta, commandName, labels)
	builder.invoke(b)
	return b.build()
}

open class CommandBuilder<context : CommandContext>(
		val loritta: LorittaBot,
		val commandName: String,
		val labels: List<String>
) {
	var canUseInPrivateChannel = false
	var needsToUploadFiles = false
	var hideInHelp = false
	var requiredFeatures = listOf<PlatformFeature>()
	var onlyOwner = false

	var descriptionCallback: ((BaseLocale) -> (String))? = null
	var usageCallback: (CommandArgumentsBuilder.() -> Unit)? = null
	var examplesCallback: ((BaseLocale) -> (List<String>))? = null
	var executeCallback: (suspend context.() -> (Unit))? = null

	fun description(callback: (BaseLocale) -> (String)) {
		this.descriptionCallback = callback
	}

	fun usage(callback: CommandArgumentsBuilder.() -> Unit) {
		this.usageCallback = callback
	}

	fun examples(callback: (BaseLocale) -> (List<String>)) {
		this.examplesCallback = callback
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
				description = descriptionCallback ?: { "???" },
				usage = usage,
				examples = examplesCallback,
				executor = executeCallback!!
		).apply { build2().invoke(this) }
	}

	fun build2(): Command<context>.() -> (Unit) {
		return {
			this.canUseInPrivateChannel = false
		}
	}
}