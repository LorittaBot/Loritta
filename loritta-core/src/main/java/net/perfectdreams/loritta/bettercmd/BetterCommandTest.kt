package net.perfectdreams.loritta.bettercmd

import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.api.commands.CommandArguments

fun command(labels: List<String>, builder: BetterCommandBuilder<ConsoleSender>.() -> (Unit)): BetterCommand<ConsoleSender> {
	val b = BetterCommandBuilder<ConsoleSender>(labels)
	builder.invoke(b)
	return b.build()
}

class BetterCommandBuilder<context: ConsoleSender>(val labels: List<String>) {
	var canUseInPrivateChannel = false
	var needsToUploadFiles = false
	var hideInHelp = false
	private var descriptionCallback: ((BaseLocale) -> (String))? = null
	private var usageCallback: (CommandArguments.(BaseLocale) -> (Unit))? = null
	private var examplesCallback: ((BaseLocale) -> (List<String>))? = null
	private var executeCallback: (suspend context.() -> (Unit))? = null

	fun description(callback: (BaseLocale) -> (String)) {
		this.descriptionCallback = callback
	}

	fun usage(callback: CommandArguments.(BaseLocale) -> (Unit)) {
		this.usageCallback = callback
	}

	fun examples(callback: (BaseLocale) -> (List<String>)) {
		this.examplesCallback = callback
	}

	fun executes(callback: suspend context.() -> (Unit)) {
		println("Callback received")
		this.executeCallback = callback
	}

	fun build(): BetterCommand<context> {
		return BetterCommand(
				labels,
				executeCallback!!
		)
	}
}

class BetterCommand<context : ConsoleSender>(
		val labels: List<String>,
		val executor: (suspend context.() -> (Unit))
) {
}

fun main() {
	val test = command(listOf("test")) {
		executes {
			send("Hello, World!")
		}
	}

	runBlocking {
		test.executor.invoke(ConsoleSender())
	}
}