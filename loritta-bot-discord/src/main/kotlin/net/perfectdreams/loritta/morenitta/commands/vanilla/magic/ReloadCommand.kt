package net.perfectdreams.loritta.morenitta.commands.vanilla.magic

import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.AbstractCommand
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.ReactionOption
import net.perfectdreams.loritta.morenitta.messages.LorittaReply
import net.perfectdreams.loritta.morenitta.website.LorittaWebsite
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteAssetsHashes
import java.io.File

class ReloadCommand(loritta: LorittaBot) : AbstractCommand(loritta, "reload", category = net.perfectdreams.loritta.common.commands.CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: BaseLocale): String {
		return "Recarrega a Loritta"
	}

	override suspend fun run(context: CommandContext,locale: BaseLocale) {
		val arg0 = context.rawArgs.getOrNull(0)
		val arg1 = context.rawArgs.getOrNull(1)
		val arg2 = context.rawArgs.getOrNull(2)

		if (arg0 == "action") {
			loritta.lorittaShards.queryAllLorittaClusters("/api/v1/loritta/action/$arg1")
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
		if (arg0 == "shard") {
			val shardId = context.rawArgs.getOrNull(1)!!.split(",").map { it.toInt() }
			shardId.forEach {
				loritta.lorittaShards.shardManager.restart(it)
			}
			context.reply(
                    LorittaReply(
                            message = "Shard $shardId está sendo reiniciada... Gotta go fast!!!"
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
			val reactionRole = loritta.pudding.transaction {
				ReactionOption.new {
					this.guildId = 297732013006389252L
					this.textChannelId = 532653936188850177L
					this.messageId = 532654456878268433L
					this.reaction = "331179879582269451"
					this.roleIds = listOf("334734175531696128")
					this.locks = listOf()
				}
			}

			context.sendMessage("Adicionado configuração genérica para o reaction role! ID: ${reactionRole.id.value}")
			return
		}

		if (arg0 == "websitekt") {
			net.perfectdreams.loritta.morenitta.website.LorittaWebsite.INSTANCE.pathCache.clear()
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

			loritta.lorittaShards.getGuilds().forEach {
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

		context.reply(
                LorittaReply(
                        "Mas... cadê o sub argumento?",
                        Emotes.LORI_SHRUG
                )
		)
	}
}