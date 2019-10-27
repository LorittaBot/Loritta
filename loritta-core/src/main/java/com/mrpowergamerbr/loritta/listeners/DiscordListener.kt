package com.mrpowergamerbr.loritta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.commands.vanilla.administration.MuteCommand
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.dao.Mute
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.modules.AutoroleModule
import com.mrpowergamerbr.loritta.modules.ReactionModule
import com.mrpowergamerbr.loritta.modules.StarboardModule
import com.mrpowergamerbr.loritta.modules.WelcomeModule
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.Mutes
import com.mrpowergamerbr.loritta.userdata.MemberCounterConfig
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.extensions.await
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.ReadyEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.api.events.http.HttpRequestEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.dao.Giveaway
import net.perfectdreams.loritta.dao.ReactionOption
import net.perfectdreams.loritta.platform.discord.plugin.DiscordPlugin
import net.perfectdreams.loritta.tables.Giveaways
import net.perfectdreams.loritta.tables.ReactionOptions
import net.perfectdreams.loritta.utils.FeatureFlags
import net.perfectdreams.loritta.utils.giveaway.GiveawayManager
import okio.Buffer
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.kotlin.utils.getOrPutNullable
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.set

class DiscordListener(internal val loritta: Loritta) : ListenerAdapter() {
	companion object {
		const val MEMBER_COUNTER_COOLDOWN = 150_000L

		/**
		 * Utilizado para não enviar mudanças do contador no event log
		 */
		val memberCounterJoinLeftCache = Collections.newSetFromMap(
				Caffeine.newBuilder()
						.expireAfterWrite(5, TimeUnit.SECONDS)
						.build<Long, Boolean>()
						.asMap()
		)
		val memberCounterLastUpdate = Caffeine.newBuilder()
				.expireAfterWrite(120, TimeUnit.SECONDS)
				.build<Long, Long>()
				.asMap()
		val memberCounterUpdateJobs = Caffeine.newBuilder()
				.expireAfterWrite(120, TimeUnit.SECONDS)
				.build<Long, Job>()
				.asMap()

		private val logger = KotlinLogging.logger {}
		private val requestLogger = LoggerFactory.getLogger("requests")

		fun queueTextChannelTopicUpdates(guild: Guild, serverConfig: MongoServerConfig, hideInEventLog: Boolean = false) {
			val donationKey = transaction(Databases.loritta) {
				loritta.getOrCreateServerConfig(guild.idLong).donationKey
			}

			logger.debug { "Creating text channel topic updates in $guild for ${guild.textChannels.size} channels! Donation key is $donationKey (${donationKey?.value}) Should hide in event log? $hideInEventLog" }

			val validChannels = guild.textChannels.filter {
				val memberCounterConfig = serverConfig.getTextChannelConfig(it).memberCounterConfig
				guild.selfMember.hasPermission(it, Permission.MANAGE_CHANNEL) && memberCounterConfig?.topic?.contains("{counter}") == true
			}

			val channelsThatWillBeChecked = if (donationKey?.isActive() == true && donationKey.value >= LorittaPrices.ALLOW_MORE_THAN_ONE_MEMBER_COUNTER && FeatureFlags.ALLOW_MORE_THAN_ONE_COUNTER_FOR_PREMIUM_USERS) {
				validChannels.take(3)
			} else {
				validChannels.take(1)
			}

			for (textChannel in channelsThatWillBeChecked) {
				queueTextChannelTopicUpdate(guild, serverConfig, donationKey, textChannel, hideInEventLog)
			}
		}

		fun queueTextChannelTopicUpdate(guild: Guild, serverConfig: MongoServerConfig, donationKey: DonationKey?, textChannel: TextChannel, hideInEventLog: Boolean = false) {
			if (!guild.selfMember.hasPermission(textChannel, Permission.MANAGE_CHANNEL))
				return

			val memberCountConfig = serverConfig.getTextChannelConfig(textChannel).memberCounterConfig ?: return

			val lastUpdate = memberCounterLastUpdate[textChannel.idLong] ?: 0L
			val diff = System.currentTimeMillis() - lastUpdate

			if (Companion.MEMBER_COUNTER_COOLDOWN > diff) { // Para evitar rate limits ao ter muitas entradas/saídas ao mesmo tempo, vamos esperar 60s entre cada update
				logger.info { "Text channel $textChannel topic is on cooldown for guild $guild, donation key is $donationKey (${donationKey?.value}), waiting ${diff}ms until next update..."}

				memberCounterLastUpdate[textChannel.idLong] = System.currentTimeMillis()
				val currentJob = Companion.memberCounterUpdateJobs[textChannel.idLong]
				currentJob?.cancel()

				memberCounterUpdateJobs[textChannel.idLong] = GlobalScope.launch(loritta.coroutineDispatcher) {
					delay(diff)

					if (!this.isActive) {
						Companion.memberCounterUpdateJobs[textChannel.idLong] = null
						return@launch
					}

					updateTextChannelTopic(guild, serverConfig, donationKey, textChannel, memberCountConfig, hideInEventLog)
					memberCounterUpdateJobs.remove(textChannel.idLong)
				}
				return
			}

			updateTextChannelTopic(guild, serverConfig, donationKey, textChannel, memberCountConfig, hideInEventLog)
		}

		fun updateTextChannelTopic(guild: Guild, serverConfig: MongoServerConfig, donationKey: DonationKey?, textChannel: TextChannel, memberCounterConfig: MemberCounterConfig, hideInEventLog: Boolean = false) {
			val formattedTopic = memberCounterConfig.getFormattedTopic(guild)
			if (hideInEventLog)
				Companion.memberCounterJoinLeftCache.add(textChannel.idLong)
			Companion.memberCounterLastUpdate[textChannel.idLong] = System.currentTimeMillis()

			val locale = loritta.getLocaleById(serverConfig.localeId)
			logger.info { "Updating text channel $textChannel topic in $guild! Donation key is $donationKey (${donationKey?.value}), hide in event log? $hideInEventLog" }
			logger.trace { "Member Counter Theme = ${memberCounterConfig.theme}"}
			logger.trace { "Member Counter Padding = ${memberCounterConfig.padding}"}
			logger.trace { "Formatted Topic = $formattedTopic" }

			if (FeatureFlags.MEMBER_COUNTER_UPDATE)
				textChannel.manager.setTopic(formattedTopic).reason(locale["loritta.modules.counter.auditLogReason"]).queue()
		}
	}

	override fun onHttpRequest(event: HttpRequestEvent) {
		val copy = event.requestRaw?.newBuilder()?.build()
		val buffer = Buffer()
		copy?.body()?.writeTo(buffer)

		val input = buffer.readUtf8()
		if (input.startsWith("--")) {
			val lines = input.lines()

			requestLogger.info("${event.route.method.name} ${event.route.compiledRoute}\n${lines.take(3).joinToString("\n")}")
		} else {
			requestLogger.info("${event.route.method.name} ${event.route.compiledRoute}\n$input")
		}
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		if (event.user.isBot)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			ReactionModule.onReactionAdd(event)
		}
	}

	override fun onGuildMessageReactionRemove(event: GuildMessageReactionRemoveEvent) {
		if (event.user.isBot)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			ReactionModule.onReactionRemove(event)
		}
	}

	override fun onGenericMessageReaction(e: GenericMessageReactionEvent) {
		if (e.user.isBot) // Ignorar reactions de bots
			return

		if (DebugLog.cancelAllEvents)
			return

		if (loritta.messageInteractionCache.containsKey(e.messageIdLong)) {
			val functions = loritta.messageInteractionCache[e.messageIdLong]!!

			if (e is MessageReactionAddEvent) {
				if (functions.onReactionAdd != null) {
					GlobalScope.launch(loritta.coroutineDispatcher) {
						try {
							functions.onReactionAdd!!.invoke(e)
						} catch (e: Exception) {
							logger.error("Erro ao tentar processar onReactionAdd", e)
						}
					}
				}

				if (e.user.id == functions.originalAuthor && (functions.onReactionAddByAuthor != null || functions.onReactionByAuthor != null)) {
					GlobalScope.launch(loritta.coroutineDispatcher) {
						try {
							functions.onReactionByAuthor?.invoke(e)
							functions.onReactionAddByAuthor?.invoke(e)
						} catch (e: Exception) {
							logger.error("Erro ao tentar processar onReactionAddByAuthor", e)
						}
					}
				}
			}

			if (e is MessageReactionRemoveEvent) {
				if (functions.onReactionRemove != null) {
					GlobalScope.launch(loritta.coroutineDispatcher) {
						try {
							functions.onReactionRemove!!.invoke(e)
						} catch (e: Exception) {
							logger.error("Erro ao tentar processar onReactionRemove", e)
						}
					}
				}

				if (e.user.id == functions.originalAuthor && (functions.onReactionRemoveByAuthor != null || functions.onReactionByAuthor != null)) {
					GlobalScope.launch(loritta.coroutineDispatcher) {
						try {
							functions.onReactionByAuthor?.invoke(e)
							functions.onReactionRemoveByAuthor?.invoke(e)
						} catch (e: Exception) {
							logger.error("Erro ao tentar processar onReactionRemoveByAuthor", e)
						}
					}
				}
			}
		}

		GlobalScope.launch {
			if (e.isFromType(ChannelType.TEXT)) {
				try {
					val conf = loritta.getServerConfigForGuild(e.guild.id)

					// Sistema de Starboard
					if (conf.starboardConfig.isEnabled) {
						StarboardModule.handleStarboardReaction(e, conf)
					}
				} catch (exception: Exception) {
					logger.error("[${e.guild.name}] Starboard ${e.member?.user?.name}", exception)
				}
			}
		}
	}

	override fun onGuildLeave(e: GuildLeaveEvent) {
		logger.info { "Someone removed me @ ${e.guild}! :(" }

		loritta.socket.socketWrapper?.syncDiscordStats()

		// Remover threads de role removal caso a Loritta tenha saido do servidor
		val toRemove = mutableListOf<String>()
		MuteCommand.roleRemovalJobs.forEach { key, value ->
			if (key.startsWith(e.guild.id)) {
				logger.debug { "Stopping mute job $value @ ${e.guild} because they removed me!" }
				value.cancel()
				toRemove.add(key)
			}
		}
		toRemove.forEach { MuteCommand.roleRemovalJobs.remove(it) }

		logger.debug { "Deleting all ${e.guild} related stuff..." }

		GlobalScope.launch(loritta.coroutineDispatcher) {
			logger.trace { "Deleting MongoDB ${e.guild} config..."}

			// Quando a Loritta sair de uma guild, automaticamente remova o ServerConfig daquele servidor
			loritta.serversColl.deleteOne(Filters.eq("_id", e.guild.id))

			transaction(Databases.loritta) {
				logger.trace { "Deleting all ${e.guild} profiles..."}

				// Deletar todos os perfis do servidor
				GuildProfiles.deleteWhere {
					GuildProfiles.guildId eq e.guild.idLong
				}

				// Deletar configurações
				logger.trace { "Deleting all ${e.guild} configurations..."}
				val serverConfig = ServerConfig.findById(e.guild.idLong)
				logger.trace { "Deleting ${e.guild} configs..."}
				val donationConfig = serverConfig?.donationConfig
				val birthdayConfig = serverConfig?.birthdayConfig

				logger.trace { "Deleting ${e.guild} config..."}
				serverConfig?.delete()
				donationConfig?.delete()
				birthdayConfig?.delete()

				logger.trace { "Deleting all ${e.guild}'s giveaways..."}
				val allGiveaways = Giveaway.find {
					Giveaways.guildId eq e.guild.idLong
				}

				logger.trace { "${e.guild} has ${allGiveaways.count()} giveaways that will be cancelled and deleted!"}

				allGiveaways.forEach {
					GiveawayManager.cancelGiveaway(it, true, true)
				}

				logger.trace { "Done! Everything related to ${e.guild} was deleted!"}
			}
		}
	}

	override fun onGuildJoin(event: GuildJoinEvent) {
		logger.info { "Someone added me @ ${event.guild}! :)" }

		loritta.socket.socketWrapper?.syncDiscordStats()

		// Vamos alterar a minha linguagem quando eu entrar em um servidor, baseando na localização dele
		val region = event.guild.region
		val regionName = region.getName()
		val serverConfig = loritta.getServerConfigForGuild(event.guild.id)

		logger.trace { "regionName = $regionName" }

		// Portuguese
		if (regionName.startsWith("Brazil")) {
			logger.debug { "Setting localeId to default at ${event.guild}, regionName = $regionName" }
			serverConfig.localeId = "default"
		} else {
			logger.debug { "Setting localeId to en-us at ${event.guild}, regionName = $regionName" }
			serverConfig.localeId = "en-us"
		}

		logger.debug { "Adding DJ permission to all roles with ADMINISTRATOR or MANAGE_SERVER permission at ${event.guild}"}

		// Adicionar a permissão de DJ para alguns cargos
		event.guild.roles.forEach {
			if (it.hasPermission(Permission.ADMINISTRATOR) || it.hasPermission(Permission.MANAGE_SERVER)) {
				serverConfig.permissionsConfig.roles[it.id] = PermissionsConfig.PermissionRole().apply {
					this.permissions.add(LorittaPermission.DJ)
				}
			}
		}

		// E depois iremos salvar a configuração do servidor
		GlobalScope.launch(loritta.coroutineDispatcher) {
			loritta save serverConfig
		}
	}

	override fun onReady(event: ReadyEvent) {
		loritta.socket.socketWrapper?.syncDiscordStats()
	}

	override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
		if (DebugLog.cancelAllEvents)
			return

		logger.debug { "${event.member} joined server ${event.guild}" }

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val conf = loritta.getServerConfigForGuild(event.guild.id)

				if (loritta.networkBanManager.checkIfUserShouldBeBanned(event.user, event.guild, conf))
					return@launch

				if (conf.miscellaneousConfig.enableQuirky && event.user.name.contains("lori", true) && MiscUtils.hasInappropriateWords(event.user.name)) { // #LoritaTambémTemSentimentos
					BanCommand.ban(
							conf,
							event.guild,
							event.guild.selfMember.user,
							com.mrpowergamerbr.loritta.utils.loritta.getLegacyLocaleById(conf.localeId),
							event.user,
							"Sim, eu também tenho sentimentos. (Usar nomes inapropriados que ofendem outros usuários!)",
							false,
							7
					)
					return@launch
				}

				queueTextChannelTopicUpdates(event.guild, conf, true)

				if (conf.autoroleConfig.isEnabled && !conf.autoroleConfig.giveOnlyAfterMessageWasSent && event.guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) { // Está ativado?
					AutoroleModule.giveRoles(event.member, conf.autoroleConfig)
				}

				if (conf.joinLeaveConfig.isEnabled) { // Está ativado?
					WelcomeModule.handleJoin(event, conf)
				}

				val mute = transaction(Databases.loritta) {
					Mute.find { (Mutes.guildId eq event.guild.idLong) and (Mutes.userId eq event.member.user.idLong) }.firstOrNull()
				}

				logger.trace { "Does ${event.member} in guild ${event.guild} has a mute status? $mute" }

				if (mute != null) {
					logger.debug { "${event.member} in guild ${event.guild} has a mute! Readding roles and recreating role removal task!" }
					val locale = loritta.getLegacyLocaleById(conf.localeId)
					val muteRole = MuteCommand.getMutedRole(event.guild, loritta.getLegacyLocaleById(conf.localeId)) ?: return@launch

					event.guild.addRoleToMember(event.member, muteRole).await()

					if (mute.isTemporary)
						MuteCommand.spawnRoleRemovalThread(event.guild, locale, event.user, mute.expiresAt!!)
				}

				loritta.pluginManager.plugins.filterIsInstance(DiscordPlugin::class.java).flatMap {
					it.onGuildMemberJoinListeners
				}.forEach {
					it.invoke(event.member, event.guild, conf)
				}
			} catch (e: Exception) {
				logger.error("[${event.guild.name}] Ao entrar no servidor ${event.user.name}", e)
			}
		}
	}

	override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
		if (DebugLog.cancelAllEvents)
			return

		logger.debug { "${event.member} left server ${event.guild}" }

		// Remover thread de role removal caso o usuário tenha saido do servidor
		val job = MuteCommand.roleRemovalJobs[event.guild.id + "#" + event.member.user.id]
		logger.debug { "Stopping mute job $job due to member guild quit" }
		job?.cancel()
		MuteCommand.roleRemovalJobs.remove(event.guild.id + "#" + event.member.user.id)

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				if (event.user.id == loritta.discordConfig.discord.clientId)
					return@launch

				val conf = loritta.getServerConfigForGuild(event.guild.id)

				DiscordListener.queueTextChannelTopicUpdates(event.guild, conf, true)

				if (conf.joinLeaveConfig.isEnabled) {
					WelcomeModule.handleLeave(event, conf)
				}

				loritta.pluginManager.plugins.filterIsInstance(DiscordPlugin::class.java).flatMap {
					it.onGuildMemberLeaveListeners
				}.forEach {
					it.invoke(event.member, event.guild, conf)
				}
			} catch (e: Exception) {
				logger.error("[${event.guild.name}] Ao sair do servidor ${event.user.name}", e)
			}
		}
	}

	override fun onGuildReady(event: GuildReadyEvent) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
			val serverConfig = loritta.getServerConfigForGuild(event.guild.id)

			val mutes = transaction(Databases.loritta) {
				Mute.find {
					(Mutes.isTemporary eq true) and (Mutes.guildId eq event.guild.idLong)
				}.toMutableList()
			}

			for (mute in mutes) {
				val guild = lorittaShards.getGuildById(mute.guildId)
				if (guild == null) {
					logger.debug { "Guild \"${mute.guildId}\" não existe ou está indisponível!" }
					continue
				}

				val member = guild.getMemberById(mute.userId) ?: continue

				logger.info("Adicionado removal thread pelo MutedUsersThread já que a guild iniciou! ~ Guild: ${mute.guildId} - User: ${mute.userId}")
				MuteCommand.spawnRoleRemovalThread(guild, loritta.getLegacyLocaleById(serverConfig.localeId), member.user, mute.expiresAt!!)
			}

			// Ao voltar, vamos reprocessar todas as reações necessárias do reaction role (desta guild)
			val reactionRoles = transaction(Databases.loritta) {
				ReactionOption.find { ReactionOptions.guildId eq event.guild.idLong }.toMutableList()
			}

			// Vamos fazer cache das mensagens para evitar pegando a mesma mensagem várias vezes
			val messages = mutableMapOf<Long, Message?>()

			for (option in reactionRoles) {
				val textChannel = event.guild.getTextChannelById(option.textChannelId) ?: continue
				val message = messages.getOrPutNullable(option.messageId) {
					try {
						textChannel.retrieveMessageById(option.messageId).await()
					} catch (e: ErrorResponseException) {
						null
					}
				}

				messages[option.messageId] = message

				if (message == null)
					continue

				// Verificar locks
				// Existem vários tipos de locks: Locks de opções (via ID), locks de mensagens (via... mensagens), etc.
				// Para ficar mais fácil, vamos verificar TODOS os locks da mensagem
				val locks = mutableListOf<ReactionOption>()

				for (lock in option.locks) {
					if (lock.contains("-")) {
						val split = lock.split("-")
						val channelOptionLock = transaction(Databases.loritta) {
							ReactionOption.find {
								(ReactionOptions.guildId eq event.guild.idLong) and
										(ReactionOptions.textChannelId eq split[0].toLong()) and
										(ReactionOptions.messageId eq split[1].toLong())
							}.toMutableList()
						}
						locks.addAll(channelOptionLock)
					} else { // Lock por option ID, esse daqui é mais complicado!
						val idOptionLock = transaction(Databases.loritta) {
							ReactionOption.find {
								(ReactionOptions.id eq lock.toLong())
							}.toMutableList()
						}
						locks.addAll(idOptionLock)
					}
				}

				// Agora nós já temos a opção desejada, só dar os cargos para o usuário!
				val roles = option.roleIds.mapNotNull { event.guild.getRoleById(it) }

				if (roles.isNotEmpty()) {
					val reaction = message.reactions.firstOrNull {
						it.reactionEmote.name == option.reaction || it.reactionEmote.emote?.id == option.reaction
					}

					if (reaction != null) { // Reaction existe!
						reaction.retrieveUsers().await().asSequence().filter { !it.isBot }.mapNotNull { event.guild.getMember(it) }.forEach {
							ReactionModule.giveRolesToMember(it, reaction, option, locks, roles)
						}
					}
				}
			}

			val allActiveGiveaways = transaction(Databases.loritta) {
				Giveaway.find { (Giveaways.guildId eq event.guild.idLong) and (Giveaways.finished eq false) }.toMutableList()
			}

			allActiveGiveaways.forEach {
				try {
					if (GiveawayManager.giveawayTasks[it.id.value] == null)
						GiveawayManager.createGiveawayJob(it)
				} catch (e: Exception) {
					logger.error(e) { "Error while creating giveaway ${it.id.value} job on guild ready ${event.guild.idLong}" }
				}
			}

			loritta.pluginManager.plugins.filterIsInstance(DiscordPlugin::class.java).flatMap {
				it.onGuildReadyListeners
			}.forEach {
				it.invoke(event.guild, serverConfig)
			}
		}
	}
}