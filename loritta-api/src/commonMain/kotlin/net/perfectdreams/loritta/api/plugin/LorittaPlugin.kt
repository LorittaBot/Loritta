package net.perfectdreams.loritta.api.plugin

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandContext

abstract class LorittaPlugin(val name: String, val loritta: LorittaBot) {
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

	fun launch(block: suspend CoroutineScope.() -> Unit): Job {
		val job = GlobalScope.launch(block = block)
		pluginTasks.add(job)
		return job
	}
}