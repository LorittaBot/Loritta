package com.mrpowergamerbr.loritta.commands.vanilla.magic

import com.fasterxml.jackson.module.kotlin.readValue
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.dao.RegisterConfig
import com.mrpowergamerbr.loritta.modules.register.RegisterHolder
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.RegisterConfigs
import com.mrpowergamerbr.loritta.threads.UpdateStatusThread
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import com.mrpowergamerbr.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.api.commands.CommandCategory
import net.perfectdreams.loritta.dao.servers.moduleconfigs.LevelConfig
import net.perfectdreams.loritta.dao.servers.moduleconfigs.ReactionOption
import net.perfectdreams.loritta.tables.servers.moduleconfigs.LevelAnnouncementConfigs
import net.perfectdreams.loritta.tables.servers.moduleconfigs.RolesByExperience
import net.perfectdreams.loritta.tables.servers.moduleconfigs.TrackedTwitterAccounts
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.levels.LevelUpAnnouncementType
import net.perfectdreams.loritta.utils.levels.RoleGiveType
import net.perfectdreams.loritta.website.utils.WebsiteAssetsHashes
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction
import twitter4j.TwitterFactory
import java.io.File

class ReloadCommand : AbstractCommand("reload", category = CommandCategory.MAGIC, onlyOwner = true) {
	override fun getDescription(locale: LegacyBaseLocale): String {
		return "Recarrega a Loritta"
	}

	override suspend fun run(context: CommandContext,locale: LegacyBaseLocale) {
		val arg0 = context.rawArgs.getOrNull(0)
		val arg1 = context.rawArgs.getOrNull(1)
		val arg2 = context.rawArgs.getOrNull(2)

		if (arg0 == "posts") {
			loritta.newWebsite?.loadBlogPosts()

			context.reply(
					LoriReply(
							"Posts recarregados!"
					)
			)
			return
		}

		if (arg0 == "action") {
			lorittaShards.queryAllLorittaClusters("/api/v1/loritta/action/$arg1")
			context.reply(
					LoriReply(
							"Enviado ação para todos os clusters!"
					)
			)
			return
		}

		if (arg0 == "emotes") {
			context.reply(
					LoriReply(
							"Recarregando emotes!"
					)
			)
			Emotes.emoteManager?.loadEmotes()
			return
		}
		if (arg0 == "dailytax") {
			context.reply(
					LoriReply(
							"Retirando granas de pessoas!"
					)
			)
			LorittaTasks.DAILY_TAX_TASK.runDailyTax(true)
			return
		}
		if (arg0 == "shard") {
			val shardId = context.rawArgs.getOrNull(1)!!.toInt()
			lorittaShards.shardManager.restart(shardId)
			context.reply(
					LoriReply(
							message = "Shard $shardId está sendo reiniciada... Gotta go fast!!!"
					)
			)
			return
		}
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
			loritta.loadLegacyLocales()
			context.reply(
					LoriReply(
							message = "Locales recarregadas!"
					)
			)
			return
		}

		if (arg0 == "commands") {
			val oldCommandCount = loritta.legacyCommandManager.commandMap.size
			LorittaLauncher.loritta.loadCommandManager()
			context.reply(
					LoriReply(
							"Comandos recarregados com sucesso! **(${loritta.legacyCommandManager.commandMap.size} comandos ativados, ${loritta.legacyCommandManager.commandMap.size - oldCommandCount} comandos adicionados)**"
					)
			)
			return
		}

		if (arg0 == "inject_unsafe") {
			val registerHolder = RegisterHolder(
					step = listOf(
							RegisterHolder.RegisterStep(
									"are u a novinha or a novinha?",
									"owo nós precisamos saber",
									"https://loritta.website/assets/img/fanarts/Loritta_Dormindo_-_Ayano.png",
									1,
									listOf(
											RegisterHolder.RegisterOption(
													"\uD83D\uDD35",
													"513303483659714586"
											),
											RegisterHolder.RegisterOption(
													"\uD83D\uDD34",
													"513303519348916224"
											)
									)
							),
							RegisterHolder.RegisterStep(
									"biscoito ou bolacha?",
									"A resposta certa é bolacha e você sabe disso",
									"https://guiadacozinha.com.br/wp-content/uploads/2016/11/torta-holandesa-facil.jpg",
									1,
									listOf(
											RegisterHolder.RegisterOption(
													"\uD83D\uDD35",
													"513303531026120704"
											),
											RegisterHolder.RegisterOption(
													"\uD83D\uDD34",
													"513303543911022593"
											)
									)
							),
							RegisterHolder.RegisterStep(
									"escolhe algo filosófico ai",
									"você pode escolher até DUAS COISAS diferentes, wow!",
									null,
									2,
									listOf(
											RegisterHolder.RegisterOption(
													"krisnite:508811243994480641",
													"513310935511728130"
											),
											RegisterHolder.RegisterOption(
													"ralseinite:508811387175436291",
													"513310965647933443"
											),
											RegisterHolder.RegisterOption(
													"vieirinha:412574915879763982",
													"513310993326014464"
											)
									)
							)
					)
			)

			transaction(Databases.loritta) {
				RegisterConfigs.deleteWhere { RegisterConfigs.id eq context.guild.idLong }
				RegisterConfig.new(context.guild.idLong) {
					this.holder = registerHolder
				}
			}

			context.sendMessage("Adicionado configuração genérica para o registro!")
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

		if (arg0 == "add_role_by_level") {
			val reactionRole = transaction(Databases.loritta) {
				RolesByExperience.insertAndGetId {
					it[RolesByExperience.guildId] = 297732013006389252L
					it[RolesByExperience.requiredExperience] = arg1!!.toLong()
					it[RolesByExperience.roles] = arrayOf(arg2!!.toLong())
				}
			}

			context.sendMessage("Adicionado configuração de nível por cargo por XP! ID: ${reactionRole.value}")
			return
		}

		if (arg0 == "inject_level_config") {
			val levelConfig = transaction(Databases.loritta) {
				LevelConfig.new {
					this.roleGiveType = RoleGiveType.STACK
					this.noXpRoles = arrayOf()
					this.noXpChannels = arrayOf()
				}
			}

			val config = loritta.getOrCreateServerConfig(context.guild.idLong)

			transaction(Databases.loritta) {
				config.levelConfig = levelConfig
			}

			context.sendMessage("Adicionado configuração de level up! ID: ${levelConfig.id.value}")
			return
		}

		if (arg0 == "inject_twitter_track") {
			val channel = arg1
			val handle = arg2

			val tf = TwitterFactory(loritta.tweetTracker.buildTwitterConfig())
			val twitter = tf.instance

			val twitterUser = twitter.users().lookupUsers(handle)[0]

			transaction(Databases.loritta) {
				TrackedTwitterAccounts.insert {
					it[TrackedTwitterAccounts.guildId] = context.guild.idLong
					it[TrackedTwitterAccounts.channelId] = arg1?.toLong() ?: 0L
					it[TrackedTwitterAccounts.twitterAccountId] = twitterUser.id
					it[TrackedTwitterAccounts.message] = "{link}"
				}
			}

			loritta.tweetTracker.updateStreams()

			context.reply(
					LoriReply(
							"Adicionado track do usuário ${twitterUser.id} para canal $channel"
					)
			)
			return
		}

		if (arg0 == "add_announcement_by_level") {
			val config = loritta.getOrCreateServerConfig(context.guild.idLong)

			val reactionRole = transaction(Databases.loritta) {
				LevelAnnouncementConfigs.insertAndGetId {
					it[levelConfig] = config.levelConfig!!.id
					it[type] = LevelUpAnnouncementType.valueOf(arg1!!)
					it[channelId] = arg2!!.toLongOrNull()
					it[message] = "{@user} Yay, você passou de {previous-level} para {level}!! Antes você tinha {previous-xp} mas devido a toda sua força e determinação você passou para {xp} experiência!"
				}
			}

			context.sendMessage("Adicionado notificação de level up por XP! ID: ${reactionRole.value}")
			return
		}

		if (arg0 == "websitekt") {
			net.perfectdreams.loritta.website.LorittaWebsite.INSTANCE.pathCache.clear()
			context.reply(
					LoriReply(
							"Views regeneradas!"
					)
			)
			return
		}

		if (arg0 == "website") {
			LorittaWebsite.kotlinTemplateCache.clear()
			LorittaWebsite.ENGINE.templateCache.invalidateAll()
			context.reply(
					LoriReply(
							"Views regeneradas!"
					)
			)
			return
		}

		if (arg0 == "webassets") {
			WebsiteAssetsHashes.websiteFileHashes.clear()
			WebsiteAssetsHashes.legacyWebsiteFileHashes.clear()

			context.reply(
					LoriReply(
							"Assets regenerados!"
					)
			)
			return
		}

		if (arg0 == "restartweb") {
			loritta.stopWebServer()
			loritta.startWebServer()

			context.reply(
					LoriReply(
							"Website reiniciando!"
					)
			)
			return
		}

		if (arg0 == "stopweb") {
			loritta.stopWebServer()

			context.reply(
					LoriReply(
							"Website desligado!"
					)
			)
			return
		}

		if (arg0 == "startweb") {
			loritta.startWebServer()

			context.reply(
					LoriReply(
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
					LoriReply(
							"Datas exportadas!"
					)
			)
			return
		}

		if (arg0 == "config") {
			val file = File(System.getProperty("conf") ?: "./loritta.conf")
			loritta.config = Constants.HOCON_MAPPER.readValue(file.readText())
			val file2 = File(System.getProperty("discordConf") ?: "./discord.conf")
			loritta.discordConfig = Constants.HOCON_MAPPER.readValue(file2.readText())

			context.reply(
					LoriReply(
							"Config recarregada!"
					)
			)
			return
		}

		context.reply(
				LoriReply(
						"Mas... cadê o sub argumento?",
						Emotes.LORI_SHRUG
				)
		)
	}
}