package net.perfectdreams.loritta.api.plugin

import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandContext

abstract class LorittaPlugin(val name: String, val loritta: LorittaBot) {
	val registeredCommands = mutableListOf<Command<CommandContext>>()

	open fun onEnable() {}
	open fun onDisable() {}

	fun registerCommands(vararg commands: Command<CommandContext>) {
		commands.forEach { registerCommand(it) }
	}

	fun registerCommand(command: Command<CommandContext>) {
		loritta.commandMap.register(command)
	}
}