package net.perfectdreams.loritta.platform.discord.legacy.plugin

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.modules.MessageReceivedModule
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.api.plugin.LorittaPlugin
import net.perfectdreams.loritta.commands.vanilla.magic.LoriToolsCommand
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.sequins.ktor.BaseRoute
import net.perfectdreams.loritta.website.utils.LorittaHtmlProvider
import net.perfectdreams.loritta.website.utils.config.types.ConfigTransformer
import java.io.File

open class LorittaDiscordPlugin(name: String, override val loritta: LorittaDiscord) : LorittaPlugin(name, loritta) {
	val routes = mutableListOf<BaseRoute>()
	val configTransformers = mutableListOf<ConfigTransformer>()
	val dataFolder by lazy { File(Loritta.FOLDER, "plugins/$name") }
	val eventListeners = mutableListOf<ListenerAdapter>()
	val loriToolsExecutors = mutableListOf<LoriToolsCommand.LoriToolsExecutor>()
	val messageReceivedModules = mutableListOf<MessageReceivedModule>()
	val messageEditedModules = mutableListOf<MessageReceivedModule>()
	var htmlProvider: LorittaHtmlProvider? = null

	fun addEventListener(eventListener: ListenerAdapter) {
		eventListeners.add(eventListener)
		lorittaShards.shardManager.addEventListener(eventListener)
	}

	fun removeEventListener(eventListener: ListenerAdapter) {
		eventListeners.remove(eventListener)
		lorittaShards.shardManager.removeEventListener(eventListener)
	}

	fun addEventListener(vararg eventListeners: ListenerAdapter) {
		eventListeners.forEach {
			addEventListener(it)
		}
	}

	fun removeEventListeners(vararg eventListeners: ListenerAdapter) {
		eventListeners.forEach {
			removeEventListener(it)
		}
	}

	fun addMessageReceivedModule(module: MessageReceivedModule) = messageReceivedModules.add(module)
	fun addMessageReceivedModules(vararg modules: MessageReceivedModule) = messageReceivedModules.addAll(modules)
	fun removeMessageReceivedModule(module: MessageReceivedModule) = messageReceivedModules.remove(module)
	fun removeMessageReceivedModules(vararg modules: MessageReceivedModule) = messageReceivedModules.removeAll(modules)
	fun addMessageEditedModule(module: MessageReceivedModule) = messageEditedModules.add(module)
	fun addMessageEditedModules(vararg modules: MessageReceivedModule) = messageEditedModules.addAll(modules)
	fun removeMessageEditedModule(module: MessageReceivedModule) = messageEditedModules.remove(module)
	fun removeMessageEditedModules(vararg modules: MessageReceivedModule) = messageEditedModules.removeAll(modules)

	override fun launch(block: suspend CoroutineScope.() -> Unit): Job {
		pluginTasks.removeAll { it.isCompleted }
		val job = GlobalScope.launch(loritta.coroutineDispatcher, block = block)
		pluginTasks.add(job)
		return job
	}
}