package net.perfectdreams.loritta.api.commands

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.perfectdreams.loritta.api.platform.PlatformFeature

fun command(labels: List<String>, builder: CommandBuilder<CommandContext>.() -> (Unit)): Command<CommandContext> {
	val b = CommandBuilder<CommandContext>(labels)
	builder.invoke(b)
	return b.build()
}

class CommandBuilder<context : CommandContext>(
		val labels: List<String>
) {
	var canUseInPrivateChannel = false
	var needsToUploadFiles = false
	var hideInHelp = false
	var requiredFeatures = listOf<PlatformFeature>()

	private var descriptionCallback: ((BaseLocale) -> (String))? = null
	private var usageCallback: (CommandArgumentsBuilder.() -> Unit)? = null
	private var examplesCallback: ((BaseLocale) -> (List<String>))? = null
	private var executeCallback: (suspend context.() -> (Unit))? = null

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
		return Command(
				labels = labels,
				description = descriptionCallback!!,
				executor = executeCallback!!
		)
	}
}