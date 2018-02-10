package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mongodb.MongoClient
import com.mongodb.MongoClientOptions
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.frontend.views.GlobalHandler
import com.mrpowergamerbr.loritta.listeners.DiscordListener
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.threads.UpdateStatusThread
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import com.mrpowergamerbr.loritta.utils.eventlog.StoredMessage
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import org.apache.commons.io.FileUtils
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.pojo.PojoCodecProvider
import java.io.File

class ReloadCommand : AbstractCommand("reload", category = CommandCategory.MAGIC) {
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
		if (context.args.isNotEmpty() && context.args[0] == "setindex") {
			UpdateStatusThread.skipToIndex = context.args[1].toInt()
			context.reply(
					LoriReply(
							message = "Index alterada!"
					)
			)
			return
		}
		if (context.args.isNotEmpty() && context.args[0] == "fan_arts") {
			loritta.loadFanArts()
			context.reply(
					LoriReply(
							message = "Fan Arts recarregadas!"
					)
			)
			return
		}
		if (context.args.isNotEmpty() && context.args[0] == "locales") {
			loritta.loadLocales()
			context.reply(
					LoriReply(
							message = "Locales recarregadas!"
					)
			)
			return
		}
		if (context.args.isNotEmpty() && context.args[0] == "commands") {
			val oldCommandCount = loritta.commandManager.commandMap.size
			LorittaLauncher.loritta.loadCommandManager()
			context.reply(
					LoriReply(
							"Comandos recarregados com sucesso! **(${loritta.commandManager.commandMap.size} comandos ativados, ${loritta.commandManager.commandMap.size - oldCommandCount} comandos adicionados)**"
					)
			)
			return
		}
		if (context.args.isNotEmpty() && context.args[0] == "website") {
			GlobalHandler.generateViews()
			context.reply(
					LoriReply(
							"Views regeneradas!"
					)
			)
			return
		}

		val oldCommandCount = loritta.commandManager.commandMap.size

		val json = FileUtils.readFileToString(File("./config.json"), "UTF-8")
		val config = Loritta.GSON.fromJson(json, LorittaConfig::class.java)
		Loritta.config = config

		loritta.generateDummyServerConfig()
		LorittaLauncher.loritta.loadCommandManager()
		loritta.loadServersFromFanClub()
		loritta.loadLocales()
		loritta.loadFanArts()

		val pojoCodecRegistry = CodecRegistries.fromRegistries(MongoClient.getDefaultCodecRegistry(),
				CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()))

		val mongoBuilder = MongoClientOptions.Builder().apply {
			connectionsPerHost(1000)
			codecRegistry(pojoCodecRegistry)
		}
		val options = mongoBuilder.build()

		loritta.mongo = MongoClient("127.0.0.1:27017", options) // Hora de iniciar o MongoClient

		val db = loritta.mongo.getDatabase("loritta")

		val dbCodec = db.withCodecRegistry(pojoCodecRegistry)

		loritta.serversColl = dbCodec.getCollection("servers", ServerConfig::class.java)
		loritta.usersColl = dbCodec.getCollection("users", LorittaProfile::class.java)
		loritta.storedMessagesColl = dbCodec.getCollection("storedmessages", StoredMessage::class.java)

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

			val discordListener = DiscordListener(loritta)
			val eventLogListener = EventLogListener(loritta)

			// Registrar novos listeners
			LorittaLauncher.loritta.lorittaShards.shards.forEach {
				it.addEventListener(discordListener)
				it.addEventListener(eventLogListener)
			}
		}

		context.reply(
				LoriReply(
						"Fui recarregada com sucesso! **(${loritta.commandManager.commandMap.size} comandos ativados, ${loritta.commandManager.commandMap.size - oldCommandCount} comandos adicionados)**"
				)
		)
	}
}