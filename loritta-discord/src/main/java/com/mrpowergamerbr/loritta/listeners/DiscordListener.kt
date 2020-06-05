package com.mrpowergamerbr.loritta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.administration.MuteCommand
import com.mrpowergamerbr.loritta.dao.Mute
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.modules.*
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.Mutes
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.*
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent
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
import net.perfectdreams.loritta.dao.servers.Giveaway
import net.perfectdreams.loritta.dao.servers.moduleconfigs.*
import net.perfectdreams.loritta.platform.discord.plugin.DiscordPlugin
import net.perfectdreams.loritta.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.tables.servers.Giveaways
import net.perfectdreams.loritta.tables.servers.ServerRolePermissions
import net.perfectdreams.loritta.tables.servers.moduleconfigs.MemberCounterChannelConfigs
import net.perfectdreams.loritta.tables.servers.moduleconfigs.ReactionOptions
import net.perfectdreams.loritta.tables.servers.moduleconfigs.WarnActions
import net.perfectdreams.loritta.utils.FeatureFlags
import net.perfectdreams.loritta.utils.ServerPremiumPlans
import net.perfectdreams.loritta.utils.giveaway.GiveawayManager
import okio.Buffer
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
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

		// You can update a channel 2 times every 10 minutes
		// https://cdn.discordapp.com/attachments/681830234168754226/716341063912128636/unknown.png
		val memberCounterLastUpdate = Caffeine.newBuilder()
				.expireAfterWrite(5L, TimeUnit.MINUTES)
				.build<Long, Long>()
				.asMap()
		val memberCounterUpdateJobs = Caffeine.newBuilder()
				.expireAfterWrite(5L, TimeUnit.MINUTES)
				.build<Long, Job>()
				.asMap()

		private val logger = KotlinLogging.logger {}
		private val requestLogger = LoggerFactory.getLogger("requests")

		fun queueTextChannelTopicUpdates(guild: Guild, serverConfig: ServerConfig, hideInEventLog: Boolean = false) {
			val activeDonationValues = loritta.getOrCreateServerConfig(guild.idLong).getActiveDonationKeysValue()

			logger.debug { "Creating text channel topic updates in $guild for ${guild.textChannels.size} channels! Donation key value is $activeDonationValues Should hide in event log? $hideInEventLog" }

			val memberCountConfigs = transaction(Databases.loritta) {
				MemberCounterChannelConfig.find {
					MemberCounterChannelConfigs.channelId inList guild.channels.map { it.idLong }
				}.toList()
			}

			val validChannels = guild.textChannels.filter { channel ->
				val memberCounterConfig = memberCountConfigs.firstOrNull { it.channelId == channel.idLong }
				memberCounterConfig != null && guild.selfMember.hasPermission(channel, Permission.MANAGE_CHANNEL) && memberCounterConfig.topic.contains("{counter}")
			}

			val channelsThatWillBeChecked = validChannels.take(ServerPremiumPlans.getPlanFromValue(activeDonationValues).memberCounterCount)

			for (textChannel in channelsThatWillBeChecked)
				queueTextChannelTopicUpdate(guild, serverConfig, textChannel, hideInEventLog)
		}

		fun queueTextChannelTopicUpdate(guild: Guild, serverConfig: ServerConfig, textChannel: TextChannel, hideInEventLog: Boolean = false) {
			if (!guild.selfMember.hasPermission(textChannel, Permission.MANAGE_CHANNEL))
				return

			val memberCountConfig = transaction(Databases.loritta) {
				MemberCounterChannelConfig.find {
					MemberCounterChannelConfigs.channelId eq textChannel.idLong
				}.firstOrNull()
			} ?: return

			val lastUpdate = memberCounterLastUpdate[textChannel.idLong] ?: 0L
			val diff = System.currentTimeMillis() - lastUpdate

			if (MEMBER_COUNTER_COOLDOWN > diff) { // Para evitar rate limits ao ter muitas entradas/saídas ao mesmo tempo, vamos esperar 60s entre cada update
				logger.info { "Text channel $textChannel topic is on cooldown for guild $guild, waiting ${diff}ms until next update..."}

				memberCounterLastUpdate[textChannel.idLong] = System.currentTimeMillis()
				val currentJob = memberCounterUpdateJobs[textChannel.idLong]
				currentJob?.cancel()

				memberCounterUpdateJobs[textChannel.idLong] = GlobalScope.launch(loritta.coroutineDispatcher) {
					delay(diff)

					if (!this.isActive) {
						memberCounterUpdateJobs[textChannel.idLong] = null
						return@launch
					}

					updateTextChannelTopic(guild, serverConfig, textChannel, memberCountConfig, hideInEventLog)
					memberCounterUpdateJobs.remove(textChannel.idLong)
				}
				return
			}

			updateTextChannelTopic(guild, serverConfig, textChannel, memberCountConfig, hideInEventLog)
		}

		fun updateTextChannelTopic(guild: Guild, serverConfig: ServerConfig, textChannel: TextChannel, memberCounterConfig: MemberCounterChannelConfig, hideInEventLog: Boolean = false) {
			val formattedTopic = memberCounterConfig.getFormattedTopic(guild)
			if (hideInEventLog)
				memberCounterJoinLeftCache.add(textChannel.idLong)
			memberCounterLastUpdate[textChannel.idLong] = System.currentTimeMillis()

			val locale = loritta.getLocaleById(serverConfig.localeId)
			logger.info { "Updating text channel $textChannel topic in $guild! Hide in event log? $hideInEventLog" }
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

			requestLogger.info("${event.route.method.name} ${event.route.compiledRoute} -> ${event.response?.code}\n${lines.take(3).joinToString("\n")}")
		} else {
			requestLogger.info("${event.route.method.name} ${event.route.compiledRoute} -> ${event.response?.code}\n$input")
		}
	}

	override fun onGuildInviteCreate(event: GuildInviteCreateEvent) {
		InviteLinkModule.cachedInviteLinks.remove(event.guild.idLong)
	}

	override fun onGuildInviteDelete(event: GuildInviteDeleteEvent) {
		InviteLinkModule.cachedInviteLinks.remove(event.guild.idLong)
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		if (event.user.isBot)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			ReactionModule.onReactionAdd(event)
		}
	}

	override fun onGuildMessageReactionRemove(event: GuildMessageReactionRemoveEvent) {
		val user = event.user ?: return

		if (user.isBot)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			ReactionModule.onReactionRemove(event)
		}
	}

	override fun onGenericMessageReaction(e: GenericMessageReactionEvent) {
		val user = e.user ?: return

		if (user.isBot) // Ignorar reactions de bots
			return

		if (DebugLog.cancelAllEvents)
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
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

				if (user.id == functions.originalAuthor && (functions.onReactionAddByAuthor != null || functions.onReactionByAuthor != null)) {
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

				if (user.id == functions.originalAuthor && (functions.onReactionRemoveByAuthor != null || functions.onReactionByAuthor != null)) {
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
					// Starboard
					val config = loritta.getOrCreateServerConfig(e.guild.idLong, true)
					val starboardConfig = config.getCachedOrRetreiveFromDatabase<StarboardConfig?>(ServerConfig::starboardConfig)

					if (starboardConfig != null && starboardConfig.enabled)
						StarboardModule.handleStarboardReaction(e, starboardConfig)
				} catch (exception: Exception) {
					logger.error("[${e.guild.name}] Starboard ${e.member?.user?.name}", exception)
				}
			}
		}
	}

	override fun onGuildLeave(e: GuildLeaveEvent) {
		logger.info { "Someone removed me @ ${e.guild}! :(" }

		loritta.cachedServerConfigs.invalidate(e.guild.idLong)

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
			transaction(Databases.loritta) {
				logger.trace { "Deleting all ${e.guild} profiles..."}

				// Deletar todos os perfis do servidor
				GuildProfiles.deleteWhere {
					GuildProfiles.guildId eq e.guild.idLong
				}

				// Deletar configurações
				logger.trace { "Deleting all ${e.guild} configurations..."}
				val serverConfig = ServerConfig.findById(e.guild.idLong)

				logger.trace { "Deleting all ${e.guild} role perms..."}
				if (serverConfig != null)
					ServerRolePermissions.deleteWhere {
						ServerRolePermissions.guild eq serverConfig.id
					}

				logger.trace { "Deleting all ${e.guild} custom commands..."}
				if (serverConfig != null)
					CustomGuildCommands.deleteWhere {
						CustomGuildCommands.guild eq serverConfig.id
					}

				val moderationConfig = serverConfig?.moderationConfig
				logger.trace { "Deleting all ${e.guild} warn actions..."}
				if (serverConfig != null && moderationConfig != null)
					WarnActions.deleteWhere {
						WarnActions.config eq moderationConfig.id
					}

				logger.trace { "Deleting all ${e.guild} member counters..."}
				if (serverConfig != null)
					MemberCounterChannelConfigs.deleteWhere {
						MemberCounterChannelConfigs.guild eq serverConfig.id
					}

				logger.trace { "Deleting ${e.guild} config..."}
				serverConfig?.delete()

				logger.trace { "Deleting all ${e.guild}'s giveaways..."}
				val allGiveaways = Giveaway.find {
					Giveaways.guildId eq e.guild.idLong
				}

				logger.trace { "${e.guild} has ${allGiveaways.count()} giveaways that will be cancelled and deleted!"}

				allGiveaways.forEach {
					GiveawayManager.cancelGiveaway(it, true, true)
				}

				Mutes.deleteWhere {
					Mutes.guildId eq e.guild.idLong
				}

				logger.trace { "Done! Everything related to ${e.guild} was deleted!"}
			}
		}
	}

	override fun onGuildJoin(event: GuildJoinEvent) {
		logger.info { "Someone added me @ ${event.guild}! :)" }

		// Vamos alterar a minha linguagem quando eu entrar em um servidor, baseando na localização dele
		val region = event.guild.region
		val regionName = region.getName()

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)
			logger.trace { "regionName = $regionName" }

			// Portuguese
			if (regionName.startsWith("Brazil")) {
				logger.debug { "Setting localeId to default at ${event.guild}, regionName = $regionName" }
				transaction(Databases.loritta) {
					serverConfig.localeId = "default"
				}
			} else {
				logger.debug { "Setting localeId to en-us at ${event.guild}, regionName = $regionName" }
				transaction(Databases.loritta) {
					serverConfig.localeId = "en-us"
				}
			}

			logger.debug { "Adding DJ permission to all roles with ADMINISTRATOR or MANAGE_SERVER permission at ${event.guild}"}

			// Adicionar a permissão de DJ para alguns cargos
			event.guild.roles.forEach { role ->
				if (role.hasPermission(Permission.ADMINISTRATOR) || role.hasPermission(Permission.MANAGE_SERVER)) {
					transaction(Databases.loritta) {
						ServerRolePermissions.insert {
							it[ServerRolePermissions.guild] = serverConfig.id
							it[ServerRolePermissions.roleId] = role.idLong
							it[ServerRolePermissions.permission] = LorittaPermission.DJ
						}
					}
				}
			}
		}
	}

	override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
		if (DebugLog.cancelAllEvents)
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		logger.debug { "${event.member} joined server ${event.guild}" }

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong, true)

				if (FeatureFlags.UPDATE_IN_GUILD_STATS_ON_GUILD_JOIN) {
					val profile = serverConfig.getUserDataIfExists(event.guild.idLong)

					if (profile != null) {
						transaction(Databases.loritta) {
							profile.isInGuild = true
						}
					}
				}

				val autoroleConfig = serverConfig.getCachedOrRetreiveFromDatabase<AutoroleConfig?>(ServerConfig::autoroleConfig)
				val welcomerConfig = serverConfig.getCachedOrRetreiveFromDatabase<WelcomerConfig?>(ServerConfig::welcomerConfig)

				queueTextChannelTopicUpdates(event.guild, serverConfig, true)

				if (autoroleConfig != null && autoroleConfig.enabled && !autoroleConfig.giveOnlyAfterMessageWasSent && event.guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) // Está ativado?
					AutoroleModule.giveRoles(event.member, autoroleConfig)

				if (welcomerConfig != null) // Está ativado?
					WelcomeModule.handleJoin(event, serverConfig, welcomerConfig)

				val mute = transaction(Databases.loritta) {
					Mute.find { (Mutes.guildId eq event.guild.idLong) and (Mutes.userId eq event.member.user.idLong) }.firstOrNull()
				}

				logger.trace { "Does ${event.member} in guild ${event.guild} has a mute status? $mute" }

				if (mute != null) {
					logger.debug { "${event.member} in guild ${event.guild} has a mute! Readding roles and recreating role removal task!" }
					val locale = loritta.getLegacyLocaleById(serverConfig.localeId)
					val muteRole = MuteCommand.getMutedRole(event.guild, loritta.getLocaleById(serverConfig.localeId)) ?: return@launch

					event.guild.addRoleToMember(event.member, muteRole).await()

					if (mute.isTemporary)
						MuteCommand.spawnRoleRemovalThread(event.guild, locale, event.user, mute.expiresAt!!)
				}

				loritta.pluginManager.plugins.filterIsInstance(DiscordPlugin::class.java).flatMap {
					it.onGuildMemberJoinListeners
				}.forEach {
					it.invoke(event.member, event.guild, serverConfig)
				}
			} catch (e: Exception) {
				logger.error("[${event.guild.name}] Ao entrar no servidor ${event.user.name}", e)
			}
		}
	}

	override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
		if (DebugLog.cancelAllEvents)
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
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

				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong, true)

				if (FeatureFlags.UPDATE_IN_GUILD_STATS_ON_GUILD_QUIT) {
					val profile = serverConfig.getUserDataIfExists(event.guild.idLong)

					if (profile != null) {
						transaction(Databases.loritta) {
							profile.isInGuild = false
						}
					}
				}

				DiscordListener.queueTextChannelTopicUpdates(event.guild, serverConfig, true)

				val welcomerConfig = serverConfig.getCachedOrRetreiveFromDatabase<WelcomerConfig?>(ServerConfig::welcomerConfig)

				if (welcomerConfig != null)
					WelcomeModule.handleLeave(event, serverConfig, welcomerConfig)

				loritta.pluginManager.plugins.filterIsInstance(DiscordPlugin::class.java).flatMap {
					it.onGuildMemberLeaveListeners
				}.forEach {
					it.invoke(event.member, event.guild, serverConfig)
				}
			} catch (e: Exception) {
				logger.error("[${event.guild.name}] Ao sair do servidor ${event.user.name}", e)
			}
		}
	}

	override fun onGuildReady(event: GuildReadyEvent) {
		val guild = event.guild

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong)

			val mutes = transaction(Databases.loritta) {
				Mute.find {
					(Mutes.isTemporary eq true) and (Mutes.guildId eq event.guild.idLong)
				}.toMutableList()
			}

			for (mute in mutes) {
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
						it.reactionEmote.name == option.reaction || it.reactionEmote.emote.id == option.reaction
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