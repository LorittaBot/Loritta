package com.mrpowergamerbr.loritta.plugin

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import net.perfectdreams.loritta.api.LorittaBot
import java.io.File
import java.net.URLClassLoader

@Deprecated("Please use LorittaPlugin")
open class LorittaPlugin(name: String, loritta: LorittaBot) : net.perfectdreams.loritta.api.plugin.LorittaPlugin(name, loritta) {
	lateinit var classLoader: URLClassLoader
	lateinit var pluginFile: File

	val dataFolder by lazy { File(Loritta.FOLDER, "plugins/$name") }
	val messageReceivedModules = mutableListOf<MessageReceivedModule>()
	val messageEditedModules = mutableListOf<MessageReceivedModule>()

	val lorittaDiscord = loritta as Loritta

	override fun onEnable() {

	}

	override fun onDisable() {
		messageReceivedModules.clear()
		messageEditedModules.clear()
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

	override fun launch(block: suspend CoroutineScope.() -> Unit): Job {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}