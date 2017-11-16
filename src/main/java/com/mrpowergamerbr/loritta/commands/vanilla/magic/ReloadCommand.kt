package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandBase
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.listeners.DiscordListener
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.listeners.UpdateTimeListener
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import com.mrpowergamerbr.loritta.utils.loritta
import org.apache.commons.io.FileUtils
import org.mongodb.morphia.Morphia
import java.io.File

class ReloadCommand : CommandBase("reload") {
	override fun getCategory(): CommandCategory {
		return CommandCategory.MAGIC
	}

	override fun onlyOwner(): Boolean {
		return true
	}

	override fun getDescription(): String {
		return "Recarrega a Loritta"
	}

	override fun run(context: CommandContext) {
		if (context.args.isNotEmpty() && context.args[0] == "info") {
			context.reply(LoriReply(
					"**Plugins:** ${loritta.pluginManager.plugins.joinToString{ it.name }}"
			))
			return
		}
		val oldCommandCount = loritta.commandManager.commandMap.size

		val json = FileUtils.readFileToString(File("./config.json"), "UTF-8")
		val config = Loritta.GSON.fromJson(json, LorittaConfig::class.java)
		Loritta.config = config

		loritta.morphia = Morphia()
		loritta.ds = LorittaLauncher.getInstance().morphia.createDatastore(LorittaLauncher.getInstance().mongo, "loritta")
		loritta.generateDummyServerConfig()
		loritta.pluginManager.clearPlugins()
		loritta.pluginManager.loadPlugins()
		LorittaLauncher.loritta.loadCommandManager()
		loritta.loadServersFromFanClub()
		loritta.loadLocales()

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
				),
				LoriReply(
						"**Plugins:** ${loritta.pluginManager.plugins.joinToString{ it.name }}",
						mentionUser = false
				)
		)
	}
}