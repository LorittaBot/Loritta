package net.perfectdreams.loritta.api.plugin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.AbstractCommandBase
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandContext

abstract class LorittaPlugin(
		val name: String,
		// Nifty trick: By keeping it "open", implementations can override this variable.
		// By doing this, classes can use their own platform implementation (example: LorittaDiscord instead of LorittaBot)
		// If you don't keep it "open", the type will always be "LorittaBot", which sucks.
		open val loritta: LorittaBot
) {
	val registeredCommands = mutableListOf<Command<CommandContext>>()
	val pluginTasks = mutableListOf<Job>()

	open fun onEnable() {}
	open fun onDisable() {}

	fun registerCommands(vararg commands: Command<CommandContext>) {
		commands.forEach { registerCommand(it) }
	}

	fun registerCommand(command: Command<CommandContext>) {
		loritta.commandMap.register(command)
		registeredCommands.add(command)
	}

	fun registerCommands(vararg commands: AbstractCommandBase<*, *>) {
		commands.forEach { registerCommand(it) }
	}

	fun registerCommand(commandBase: AbstractCommandBase<*, *>) {
		val command = commandBase.command()
		loritta.commandMap.register(command as Command<CommandContext>)
		registeredCommands.add(command)
	}

	// Plataformas deverão fazer override disto, já que não é bom usar GlobalScope sem um coroutine dispatcher
	open fun launch(block: suspend CoroutineScope.() -> Unit): Job {
		pluginTasks.removeAll { it.isCompleted }
		val job = GlobalScope.launch(block = block)
		pluginTasks.add(job)
		return job
	}
}