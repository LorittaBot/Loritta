package com.mrpowergamerbr.loritta.plugin

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.platform.LorittaBot
import org.jooby.Jooby
import java.io.File
import java.net.URLClassLoader

open class LorittaPlugin {
	lateinit var loritta: LorittaBot
	lateinit var name: String
	lateinit var classLoader: URLClassLoader
	lateinit var pluginFile: File

	val commands = mutableListOf<LorittaCommand>()
	val dataFolder by lazy { File(Loritta.FOLDER, "plugins/$name") }
	val messageReceivedModules = mutableListOf<MessageReceivedModule>()
	val messageEditedModules = mutableListOf<MessageReceivedModule>()
	val routes = mutableListOf<Jooby>()

	open fun onEnable() {

	}

	open fun onDisable() {
		messageReceivedModules.clear()
		messageEditedModules.clear()
		routes.clear()
	}

	fun registerCommand(command: LorittaCommand) {
		loritta.commandManager.registerCommand(command)
		commands.add(command)
	}

	fun registerMessageReceivedModule(module: MessageReceivedModule) {
		messageReceivedModules.add(module)
	}

	fun registerMessageReceivedModules(vararg modules: MessageReceivedModule) {
		messageReceivedModules.addAll(modules)
	}

	fun unregisterMessageReceivedModule(module: MessageReceivedModule) {
		messageReceivedModules.remove(module)
	}

	fun unregisterMessageReceivedModules(vararg modules: MessageReceivedModule) {
		messageReceivedModules.removeAll(modules)
	}

	fun registerMessageEditedModule(module: MessageReceivedModule) {
		messageEditedModules.add(module)
	}

	fun registerMessageEditedModules(vararg modules: MessageReceivedModule) {
		messageEditedModules.addAll(modules)
	}

	fun unregisterMessageEditedModule(module: MessageReceivedModule) {
		messageEditedModules.remove(module)
	}

	fun unregisterMessageEditedModules(vararg modules: MessageReceivedModule) {
		messageEditedModules.removeAll(modules)
	}
}