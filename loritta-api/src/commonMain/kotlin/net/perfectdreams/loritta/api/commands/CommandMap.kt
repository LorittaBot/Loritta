package net.perfectdreams.loritta.api.commands

interface CommandMap<Cmd: Command<*>> {
	fun register(command: Cmd)

	fun registerAll(vararg commands: Cmd) {
		commands.forEach { register(it) }
	}

	fun unregister(command: Cmd)

	fun unregisterAll(vararg commands: Cmd) {
		commands.forEach { unregister(it) }
	}
}