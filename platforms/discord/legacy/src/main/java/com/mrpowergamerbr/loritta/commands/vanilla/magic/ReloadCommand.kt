package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.threads.UpdateStatusThread
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.LorittaTasks
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.lorittaShards
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.common.commands.CommandCategory
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.dao.servers.moduleconfigs.ReactionOption
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.HoconUtils.decodeFromFile
import net.perfectdreams.loritta.website.utils.WebsiteAssetsHashes
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File

class ReloadCommand : AbstractCommand("reload", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Recarrega a Loritta"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val arg0 = context.rawArgs.getOrNull(0)
		val arg1 = context.rawArgs.getOrNull(1)
		val arg2 = context.rawArgs.getOrNull(2)

		if (arg0 == "posts") {
			loritta.newWebsite?.loadBlogPosts()

			context.reply(
                    LorittaReply(
                            "Posts recarregados!"
                    )
			)
			return
		}

		if (arg0 == "action") {
			lorittaShards.queryAllLorittaClusters("/api/v1/loritta/action/$arg1")
			context.reply(
                    LorittaReply(
                            "Enviado ação para todos os clusters!"
                    )
			)
			return
		}

		if (arg0 == "emotes") {
			context.reply(
                    LorittaReply(
                            "Recarregando emotes!"
                    )
			)
			Emotes.emoteManager?.loadEmotes()
			return
		}
		if (arg0 == "dailytax") {
			context.reply(
                    LorittaReply(
                            "Retirando granas de pessoas!"
                    )
			)
			LorittaTasks.DAILY_TAX_TASK.runDailyTax(true)
			return
		}
		if (arg0 == "shard") {
			val shardId = context.rawArgs.getOrNull(1)!!.split(",").map { it.toInt() }
			shardId.forEach {
				lorittaShards.shardManager.restart(it)
			}
			context.reply(
                    LorittaReply(
                            message = "Shard $shardId está sendo reiniciada... Gotta go fast!!!"
                    )
			)
			return
		}
		if (arg0 == "setindex") {
			UpdateStatusThread.skipToIndex = context.args[1].toInt()
			context.reply(
                    LorittaReply(
                            message = "Index alterada!"
                    )
			)
			return
		}
		if (arg0 == "fan_arts" || arg0 == "fanarts") {
			loritta.loadFanArts()
			context.reply(
                    LorittaReply(
                            message = "Fan Arts recarregadas!"
                    )
			)
			return
		}
		if (arg0 == "locales") {
			loritta.localeManager.loadLocales()
			loritta.loadLegacyLocales()
			context.reply(
                    LorittaReply(
                            message = "Locales recarregadas!"
                    )
			)
			return
		}

		if (arg0 == "inject_unsafe2") {
			val reactionRole = transaction(Databases.loritta) {
				ReactionOption.new {
					this.guildId = 297732013006389252L
					this.textChannelId = 532653936188850177L
					this.messageId = 532654456878268433L
					this.reaction = "331179879582269451"
					this.roleIds = arrayOf("334734175531696128")
					this.locks = arrayOf()
				}
			}

			context.sendMessage("Adicionado configuração genérica para o reaction role! ID: ${reactionRole.id.value}")
			return
		}

		if (arg0 == "websitekt") {
			net.perfectdreams.loritta.website.LorittaWebsite.INSTANCE.pathCache.clear()
			context.reply(
                    LorittaReply(
                            "Views regeneradas!"
                    )
			)
			return
		}

		if (arg0 == "website") {
			LorittaWebsite.ENGINE.templateCache.invalidateAll()
			context.reply(
                    LorittaReply(
                            "Views regeneradas!"
                    )
			)
			return
		}

		if (arg0 == "webassets") {
			WebsiteAssetsHashes.websiteFileHashes.clear()
			WebsiteAssetsHashes.legacyWebsiteFileHashes.clear()

			context.reply(
                    LorittaReply(
                            "Assets regenerados!"
                    )
			)
			return
		}

		if (arg0 == "restartweb") {
			loritta.stopWebServer()
			loritta.startWebServer()

			context.reply(
                    LorittaReply(
                            "Website reiniciando!"
                    )
			)
			return
		}

		if (arg0 == "stopweb") {
			loritta.stopWebServer()

			context.reply(
                    LorittaReply(
                            "Website desligado!"
                    )
			)
			return
		}

		if (arg0 == "startweb") {
			loritta.startWebServer()

			context.reply(
                    LorittaReply(
                            "Website ligado!"
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
				val year = self.timeJoined.year
				val month = self.timeJoined.monthValue

				val padding = month.toString().padStart(2, '0')
				dates.put("$year-$padding", dates.getOrDefault("$year-$padding", 0) + 1)
			}

			val sorted = dates.entries.sortedBy { it.key }

			val servers = sorted.sumBy { it.value }

			file.writeText(sorted.joinToString("\n", transform = { it.key + " - " + it.value + " servidores"}) + "\n\nTotal: ${servers} servidores")

			context.reply(
                    LorittaReply(
                            "Datas exportadas!"
                    )
			)
			return
		}

		if (arg0 == "config") {
			val file = File(System.getProperty("conf") ?: "./loritta.conf")
			loritta.config = Constants.HOCON.decodeFromFile(file)
			val file2 = File(System.getProperty("discordConf") ?: "./discord.conf")
			loritta.discordConfig = Constants.HOCON.decodeFromFile(file2)

			context.reply(
                    LorittaReply(
                            "Config recarregada!"
                    )
			)
			return
		}

		context.reply(
                LorittaReply(
                        "Mas... cadê o sub argumento?",
                        Emotes.LORI_SHRUG
                )
		)
	}
}