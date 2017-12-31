package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.frontend.views.GlobalHandler
import com.mrpowergamerbr.loritta.listeners.DiscordListener
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.listeners.UpdateTimeListener
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import com.mrpowergamerbr.loritta.utils.eventlog.StoredMessage
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import org.apache.commons.io.FileUtils
import org.mongodb.morphia.Morphia
import java.io.File

class ReloadCommand : AbstractCommand("reload") {
	override fun getCategory(): CommandCategory {
		return CommandCategory.MAGIC
	}

	override fun onlyOwner(): Boolean {
		return true
	}

	override fun getDescription(): String {
		return "Recarrega a Loritta"
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		if (context.args.isNotEmpty() && context.args[0] == "dump_threads") {
			var threadCount = 0
			val threadSet = Thread.getAllStackTraces().keys
			for (t in threadSet) {
				if (t.threadGroup === Thread.currentThread().threadGroup) {
					File(Loritta.FOLDER, "thread_dump.txt").appendText("Thread :" + t + ":" + "state:" + t.state + "\n")
					++threadCount
				}
			}
			context.reply(
					LoriReply(
							message = "Threads dumpadas com sucesso! Número de threads: " + threadCount
					)
			)
			return
		}
		val oldCommandCount = loritta.commandManager.commandMap.size

		val json = FileUtils.readFileToString(File("./config.json"), "UTF-8")
		val config = Loritta.GSON.fromJson(json, LorittaConfig::class.java)
		Loritta.config = config

		val morphia = Morphia() // E o Morphia

		// tell Morphia where to find your classes
		// can be called multiple times with different packages or classes
		morphia.mapPackage("com.mrpowergamerbr.loritta.userdata")
		morphia.mapPackage("com.mrpowergamerbr.loritta.utils.eventlog")
		morphia.map(ServerConfig::class.java)
		morphia.map(LorittaProfile::class.java)
		morphia.map(StoredMessage::class.java)

		val ds = morphia.createDatastore(loritta.mongo, "loritta") // E também crie uma datastore (tudo da Loritta será salvo na database "loritta")
		ds.ensureIndexes()

		loritta.morphia = morphia
		loritta.ds = ds

		loritta.generateDummyServerConfig()
		LorittaLauncher.loritta.loadCommandManager()
		loritta.loadServersFromFanClub()
		loritta.loadLocales()
		loritta.loadFanArts()

		GlobalHandler.generateViews()

		if (context.args.isNotEmpty() && context.args[0] == "listeners") {
			context.sendMessage(context.getAsMention(true) + "Recarregando listeners...")

			// Desregistrar listeners
			LorittaLauncher.loritta.lorittaShards.shards.forEach {
				val shard = it;
				it.registeredListeners.forEach {
					shard.removeEventListener(it)
				}
			}

			val updateTimeListener = UpdateTimeListener(loritta);
			val discordListener = DiscordListener(loritta)
			val eventLogListener = EventLogListener(loritta)

			// Registrar novos listeners
			LorittaLauncher.loritta.lorittaShards.shards.forEach {
				it.addEventListener(discordListener)
				it.addEventListener(eventLogListener)
				it.addEventListener(updateTimeListener)
			}
		}

		context.reply(
				LoriReply(
						"Fui recarregada com sucesso! **(${loritta.commandManager.commandMap.size} comandos ativados, ${loritta.commandManager.commandMap.size - oldCommandCount} comandos adicionados)**"
				)
		)
	}
}