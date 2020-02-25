package net.perfectdreams.loritta.platform.discord.plugin

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.api.LorittaBot
import net.perfectdreams.loritta.api.plugin.LorittaPlugin
import net.perfectdreams.loritta.commands.vanilla.magic.LoriToolsCommand
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.routes.BaseRoute
import net.perfectdreams.loritta.website.utils.config.types.ConfigTransformer
import org.jetbrains.exposed.sql.Column
import java.io.File

open class LorittaDiscordPlugin(name: String, loritta: LorittaBot) : LorittaPlugin(name, loritta) {
	val lorittaDiscord = loritta as LorittaDiscord
	val routes = mutableListOf<BaseRoute>()
	val configTransformers = mutableListOf<ConfigTransformer>()
	val serverConfigColumns = mutableListOf<Column<out Any?>>()
	val dataFolder by lazy { File(Loritta.FOLDER, "plugins/$name") }
	val eventListeners = mutableListOf<ListenerAdapter>()
	val loriToolsExecutors = mutableListOf<LoriToolsCommand.LoriToolsExecutor>()

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

	override fun launch(block: suspend CoroutineScope.() -> Unit): Job {
		pluginTasks.removeAll { it.isCompleted }
		val job = GlobalScope.launch((lorittaDiscord as Loritta).coroutineDispatcher, block = block)
		pluginTasks.add(job)
		return job
	}
}