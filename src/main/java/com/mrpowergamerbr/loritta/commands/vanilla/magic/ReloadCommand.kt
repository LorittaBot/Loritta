package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.google.gson.Gson
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandCategory
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.listeners.DiscordListener
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.threads.UpdateStatusThread
import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.config.LorittaConfig
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import com.mrpowergamerbr.loritta.website.views.GlobalHandler
import net.dv8tion.jda.core.entities.Guild
import org.apache.commons.io.FileUtils
import java.io.File
import kotlin.concurrent.thread

class ReloadCommand : AbstractCommand("reload", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Recarrega a Loritta"
	}

	override fun run(context: CommandContext, locale: BaseLocale) {
		val arg0 = context.rawArgs.getOrNull(0)

		if (arg0 == "setindex") {
			UpdateStatusThread.skipToIndex = context.args[1].toInt()
			context.reply(
					LoriReply(
							message = "Index alterada!"
					)
			)
			return
		}
		if (arg0 == "fan_arts" || arg0 == "fanarts") {
			loritta.loadFanArts()
			context.reply(
					LoriReply(
							message = "Fan Arts recarregadas!"
					)
			)
			return
		}
		if (arg0 == "locales") {
			loritta.loadLocales()
			context.reply(
					LoriReply(
							message = "Locales recarregadas!"
					)
			)
			return
		}

		if (arg0 == "commands") {
			val oldCommandCount = loritta.commandManager.commandMap.size
			LorittaLauncher.loritta.loadCommandManager()
			context.reply(
					LoriReply(
							"Comandos recarregados com sucesso! **(${loritta.commandManager.commandMap.size} comandos ativados, ${loritta.commandManager.commandMap.size - oldCommandCount} comandos adicionados)**"
					)
			)
			return
		}

		if (arg0 == "website") {
			GlobalHandler.generateViews()
			context.reply(
					LoriReply(
							"Views regeneradas!"
					)
			)
			return
		}

		if (arg0 == "fullwebsite" || arg0 == "full_website") {
			logger.info("Parando o Jooby...")
			loritta.website.stop()
			logger.info("Interrompendo a Thread do Website...")
			loritta.websiteThread.interrupt()
			logger.info("Iniciando instância do Website...")
			loritta.website = LorittaWebsite(Loritta.config.websiteUrl, Loritta.config.frontendFolder)
			logger.info("Iniciando website...")
			loritta.websiteThread = thread(true, name = "Website Thread") {
				loritta.website = LorittaWebsite(Loritta.config.websiteUrl, Loritta.config.frontendFolder)
				org.jooby.run({
					loritta.website
				})
			}
			context.reply(
					LoriReply(
							"Full website reload completado!"
					)
			)
			return
		}

		if (arg0 == "mongo") {
			loritta.initMongo()
			context.reply(
					LoriReply(
							"MongoDB recarregado!"
					)
			)
			return
		}
		if (arg0 == "savekeys") {
			loritta.savePremiumKeys()
			context.reply(
					LoriReply(
							"Premium Keys salvas!"
					)
			)
			return
		}
		if (arg0 == "loadkeys") {
			loritta.loadPremiumKeys()
			context.reply(
					LoriReply(
							"Premium Keys carregadas!"
					)
			)
			return
		}

		if (arg0 == "exportdate") {
			val dates = mutableMapOf<String, Int>()

			val file = File("./date_export.txt")
			file.delete()

			lorittaShards.getGuilds().forEach {
				val self = it.selfMember
				val year = self.joinDate.year
				val month = self.joinDate.monthValue

				val padding = month.toString().padStart(2, '0')
				dates.put("$year-$padding", dates.getOrDefault("$year-$padding", 0) + 1)
			}

			val sorted = dates.entries.sortedBy { it.key }

			val servers = sorted.sumBy { it.value }

			file.writeText(sorted.joinToString("\n", transform = { it.key + " - " + it.value + " servidores"}) + "\n\nTotal: ${servers} servidores")

			context.reply(
					LoriReply(
							"Datas exportadas!"
					)
			)
			return
		}

		if (arg0 == "config") {
			val json = FileUtils.readFileToString(File("./config.json"), "UTF-8")
			val config = Gson().fromJson(json, LorittaConfig::class.java)
			Loritta.config = config
			context.reply(
					LoriReply(
							"Config recarregada!"
					)
			)
			return
		}

		if (arg0 == "queryuseless") {
			val uselessServers = loritta.serversColl.find(
					Filters.lt("lastCommandReceivedAt", System.currentTimeMillis() - 2592000000L)
			)

			val reallyUselessServers = mutableListOf<Guild>()

			var str = ""

			for (serverConfig in uselessServers) {
				val guild = lorittaShards.getGuildById(serverConfig.guildId) ?: continue

				// AMINO
				if (serverConfig.aminoConfig.fixAminoImages || serverConfig.aminoConfig.aminos.isNotEmpty())
					continue

				// INVITE BLOCKER
				if (serverConfig.inviteBlockerConfig.isEnabled)
					continue

				// AUTOROLE
				if (serverConfig.autoroleConfig.isEnabled)
					continue

				// EVENT LOG
				if (serverConfig.eventLogConfig.isEnabled)
					continue

				// JOIN/LEAVE
				if (serverConfig.joinLeaveConfig.isEnabled)
					continue

				// LIVESTREAM
				if (serverConfig.livestreamConfig.channels.isNotEmpty())
					continue

				// MUSIC
				if (serverConfig.musicConfig.isEnabled)
					continue

				// FEEDS
				if (serverConfig.rssFeedConfig.feeds.isNotEmpty())
					continue

				// YOUTUBE
				if (serverConfig.youTubeConfig.channels.isNotEmpty())
					continue

				reallyUselessServers.add(guild)
			}

			context.reply("Existem ${reallyUselessServers.size} servidores inúteis!")

			if (context.rawArgs.getOrNull(1) == "leave") {
				context.reply("...e eu irei sair de todos eles!")

				for (guild in reallyUselessServers) {
					guild.leave().queue()
				}
			} else {
				for (guild in reallyUselessServers.sortedByDescending { it.members.size }) {
					str += "[${guild.id}] ${guild.name} - ${guild.members.size} membros (${guild.members.count { it.user.isBot }} bots)\n"
				}

				File("/home/servers/loritta/useless-servers.txt").writeText(str)
			}
			return
		}
		val oldCommandCount = loritta.commandManager.commandMap.size

		val json = FileUtils.readFileToString(File("./config.json"), "UTF-8")
		val config = Gson().fromJson(json, LorittaConfig::class.java)
		Loritta.config = config

		loritta.generateDummyServerConfig()
		LorittaLauncher.loritta.loadCommandManager()
		loritta.loadLocales()
		loritta.loadFanArts()

		loritta.initMongo()

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