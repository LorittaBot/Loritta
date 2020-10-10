package net.perfectdreams.loritta.api.commands

interface CommandMap<Cmd: Command<CommandContext>> {
	fun register(command: Cmd)

	fun registerAll(vararg commands: Cmd) {
		commands.forEach { register(it) }
	}

	fun registerAll(vararg commands: AbstractCommandBase<*, *>) {
		commands.forEach { register(it) }
	}

	fun register(commandBase: AbstractCommandBase<*, *>) {
		val command = commandBase.command()
		register(command as Cmd)
	}

	fun unregister(command: Cmd)

	fun unregisterAll(vararg commands: Cmd) {
		commands.forEach { unregister(it) }
	}
}