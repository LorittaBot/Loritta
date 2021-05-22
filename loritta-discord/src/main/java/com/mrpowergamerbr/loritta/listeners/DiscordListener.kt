package com.mrpowergamerbr.loritta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.administration.MuteCommand
import com.mrpowergamerbr.loritta.dao.Mute
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.modules.*
import com.mrpowergamerbr.loritta.tables.DonationKeys
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.Mutes
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.loritta
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.TextChannel
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.http.HttpRequestEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionAddEvent
import net.dv8tion.jda.api.events.message.guild.react.GuildMessageReactionRemoveEvent
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.dao.servers.Giveaway
import net.perfectdreams.loritta.dao.servers.moduleconfigs.*
import net.perfectdreams.loritta.platform.discord.legacy.plugin.DiscordPlugin
import net.perfectdreams.loritta.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.tables.servers.Giveaways
import net.perfectdreams.loritta.tables.servers.ServerRolePermissions
import net.perfectdreams.loritta.tables.servers.moduleconfigs.MemberCounterChannelConfigs
import net.perfectdreams.loritta.tables.servers.moduleconfigs.ModerationPunishmentMessagesConfig
import net.perfectdreams.loritta.tables.servers.moduleconfigs.WarnActions
import net.perfectdreams.loritta.utils.ServerPremiumPlans
import net.perfectdreams.loritta.utils.giveaway.GiveawayManager
import okio.Buffer
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import kotlin.collections.set

class DiscordListener(internal val loritta: Loritta) : ListenerAdapter() {
	companion object {
		// You can update a channel 2 times every 10 minutes
		// https://cdn.discordapp.com/attachments/681830234168754226/716341063912128636/unknown.png
		private const val MEMBER_COUNTER_COOLDOWN = 300_000L // 5 minutes in ms

		val memberCounterLastUpdate = Caffeine.newBuilder()
				.expireAfterWrite(15L, TimeUnit.MINUTES)
				.build<Long, Long>()
				.asMap()

		/**
		 * Stores the member counter executing update mutexes, used when a topic update is being executed.
		 */
		val memberCounterExecutingUpdatesMutexes = Caffeine.newBuilder()
				.expireAfterWrite(15L, TimeUnit.MINUTES)
				.build<Long, Mutex>()
				.asMap()

		private val logger = KotlinLogging.logger {}
		private val requestLogger = LoggerFactory.getLogger("requests")

		suspend fun queueTextChannelTopicUpdates(guild: Guild, serverConfig: ServerConfig) {
			val activeDonationValues = loritta.getOrCreateServerConfig(guild.idLong).getActiveDonationKeysValue()

			logger.debug { "Creating text channel topic updates in $guild for ${guild.textChannels.size} channels! Donation key value is $activeDonationValues" }

			val memberCountConfigs = loritta.newSuspendedTransaction {
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
				GlobalScope.launch(loritta.coroutineDispatcher) {
					queueTextChannelTopicUpdate(guild, serverConfig, textChannel)
				}
		}

		private suspend fun queueTextChannelTopicUpdate(guild: Guild, serverConfig: ServerConfig, textChannel: TextChannel) {
			if (!guild.selfMember.hasPermission(textChannel, Permission.MANAGE_CHANNEL))
				return

			val memberCountConfig = loritta.newSuspendedTransaction {
				MemberCounterChannelConfig.find {
					MemberCounterChannelConfigs.channelId eq textChannel.idLong
				}.firstOrNull()
			} ?: return

			val memberCounterPendingForUpdateMutex = memberCounterExecutingUpdatesMutexes.getOrPut(textChannel.idLong) { Mutex() }
			val memberCounterExecutingUpdateMutex = memberCounterExecutingUpdatesMutexes.getOrPut(textChannel.idLong) { Mutex() }

			if (memberCounterPendingForUpdateMutex.isLocked) {
				// If the "memberCounterPendingForUpdateMutex" is locked, then it means that we already have a job waiting for the counter to be updated!
				// So we are going to return, the counter will be updated later when the mutex is unlocked so... whatever.
				logger.info { "Text channel $textChannel topic already has a pending update for guild $guild, cancelling..." }
				return
			}

			val diff = System.currentTimeMillis() - (memberCounterLastUpdate[textChannel.idLong] ?: 0)

			if (memberCounterExecutingUpdateMutex.isLocked) {
				// If the "memberCounterExecutingUpdateMutex" is locked, then it means that the counter is still updating!
				// We will wait until it is finished and then continue.
				logger.info { "Text channel $textChannel topic already has a pending execute for guild $guild, waiting until the update is executed to continue..." }

				// Double locc time
				memberCounterPendingForUpdateMutex.withLock {
					memberCounterExecutingUpdateMutex.withLock {
						delayForTextChannelUpdateCooldown(textChannel, diff)

						updateTextChannelTopic(guild, serverConfig, textChannel, memberCountConfig)
					}
				}
				return
			}

			memberCounterExecutingUpdateMutex.withLock {
				delayForTextChannelUpdateCooldown(textChannel, diff)

				updateTextChannelTopic(guild, serverConfig, textChannel, memberCountConfig)
			}
		}

		private suspend fun delayForTextChannelUpdateCooldown(textChannel: TextChannel, diff: Long) {
			if (MEMBER_COUNTER_COOLDOWN > diff) { // We are also going to offset the update for about ~5m, since we can only update a channel every 5m!
				logger.info { "Waiting ${diff}ms until the next $textChannel topic update..." }

				delay(MEMBER_COUNTER_COOLDOWN - diff)
			}
		}

		private suspend fun updateTextChannelTopic(guild: Guild, serverConfig: ServerConfig, textChannel: TextChannel, memberCounterConfig: MemberCounterChannelConfig) {
			val formattedTopic = memberCounterConfig.getFormattedTopic(guild)

			val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
			logger.info  { "Updating text channel $textChannel topic in $guild!" }
			logger.trace { "Member Counter Theme = ${memberCounterConfig.theme}"}
			logger.trace { "Member Counter Padding = ${memberCounterConfig.padding}"}
			logger.trace { "Formatted Topic = $formattedTopic" }

			// The reason we use ".await()" is so we can track when the request is successfully sent!
			// And, if the request is rate limited, it will take more time to be processed, which is perfect for us!
			textChannel.manager.setTopic(formattedTopic).reason(locale["loritta.modules.counter.auditLogReason"]).await()
			memberCounterLastUpdate[textChannel.idLong] = System.currentTimeMillis()
		}
	}

	override fun onHttpRequest(event: HttpRequestEvent) {
		val copy = event.requestRaw?.newBuilder()?.build()

		val body = copy?.body()
		val originalMediaType = body?.contentType()
		val mediaType = "${originalMediaType?.type()}/${originalMediaType?.subtype()}"

		if (mediaType == "application/json") {
			// We will only write the content if the input is "application/json"
			//
			// Because if we write every body, this also includes images... And that causes Humongous Allocations (a lot of memory used)! And that's bad!!
			val buffer = Buffer()
			copy?.body()?.writeTo(buffer)

			val input = buffer.readUtf8()
			val length = body?.contentLength()

			requestLogger.info("${event.route.method.name} ${event.route.compiledRoute} Media Type: $mediaType; Content Length: $length; -> ${event.response?.code}\n$input")
		} else if (originalMediaType != null) {
			requestLogger.info("${event.route.method.name} ${event.route.compiledRoute} Media Type: $mediaType; -> ${event.response?.code}")
		} else {
			requestLogger.info("${event.route.method.name} ${event.route.compiledRoute} -> ${event.response?.code}")
		}
	}

	override fun onGuildInviteCreate(event: GuildInviteCreateEvent) {
		InviteLinkModule.cachedInviteLinks.remove(event.guild.idLong)
	}

	override fun onGuildInviteDelete(event: GuildInviteDeleteEvent) {
		InviteLinkModule.cachedInviteLinks.remove(event.guild.idLong)
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		val user = event.user

		if (loritta.discordConfig.discord.disallowBots && !loritta.discordConfig.discord.botWhitelist.contains(user.idLong) && user.isBot) // Se uma mensagem de um bot, ignore a mensagem!
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			ReactionModule.onReactionAdd(event)
		}
	}

	override fun onGuildMessageReactionRemove(event: GuildMessageReactionRemoveEvent) {
		val user = event.user ?: return

		if (loritta.discordConfig.discord.disallowBots && !loritta.discordConfig.discord.botWhitelist.contains(user.idLong) && user.isBot) // Se uma mensagem de um bot, ignore a mensagem!
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			ReactionModule.onReactionRemove(event)
		}
	}

	override fun onGenericMessageReaction(e: GenericMessageReactionEvent) {
		val user = e.user ?: return

		if (loritta.discordConfig.discord.disallowBots && !loritta.discordConfig.discord.botWhitelist.contains(user.idLong) && user.isBot) // Se uma mensagem de um bot, ignore a mensagem!
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

				if (user.idLong == functions.originalAuthor && (functions.onReactionAddByAuthor != null || functions.onReactionByAuthor != null)) {
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

				if (user.idLong == functions.originalAuthor && (functions.onReactionRemoveByAuthor != null || functions.onReactionByAuthor != null)) {
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
			loritta.newSuspendedTransaction {
				logger.trace { "Deleting all ${e.guild} profiles..."}

				// Deletar todos os perfis do servidor
				GuildProfiles.deleteWhere {
					GuildProfiles.guildId eq e.guild.idLong
				}

				// Deletar configurações
				logger.trace { "Deleting all ${e.guild} configurations..." }
				val serverConfig = ServerConfig.findById(e.guild.idLong)

				logger.trace { "Removing all donation keys references about ${e.guild}..." }
				if (serverConfig != null) {
					DonationKeys.update({ DonationKeys.activeIn eq serverConfig.id }) {
						it[activeIn] = null
					}

					logger.trace { "Deleting all ${e.guild} role perms..." }

					ServerRolePermissions.deleteWhere {
						ServerRolePermissions.guild eq serverConfig.id
					}

					logger.trace { "Deleting all ${e.guild} custom commands..." }

					CustomGuildCommands.deleteWhere {
						CustomGuildCommands.guild eq serverConfig.id
					}

					val moderationConfig = serverConfig?.moderationConfig
					logger.trace { "Deleting all ${e.guild} warn actions..." }
					if (moderationConfig != null)
						WarnActions.deleteWhere {
							WarnActions.config eq moderationConfig.id
						}

					logger.trace { "Deleting all ${e.guild} member counters..." }

					MemberCounterChannelConfigs.deleteWhere {
						MemberCounterChannelConfigs.guild eq serverConfig.id
					}

					logger.trace { "Deleting all ${e.guild} moderation messages counters..." }

					ModerationPunishmentMessagesConfig.deleteWhere {
						ModerationPunishmentMessagesConfig.guild eq serverConfig.id
					}
				}

				logger.trace { "Deleting ${e.guild} config..." }
				serverConfig?.delete()

				logger.trace { "Deleting all ${e.guild}'s giveaways..." }
				val allGiveaways = Giveaway.find {
					Giveaways.guildId eq e.guild.idLong
				}

				logger.trace { "${e.guild} has ${allGiveaways.count()} giveaways that will be cancelled and deleted!" }

				allGiveaways.forEach {
					GiveawayManager.cancelGiveaway(it, true, true)
				}

				Mutes.deleteWhere {
					Mutes.guildId eq e.guild.idLong
				}

				logger.trace { "Done! Everything related to ${e.guild} was deleted!" }
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
				loritta.newSuspendedTransaction {
					serverConfig.localeId = "default"
				}
			} else {
				logger.debug { "Setting localeId to en-us at ${event.guild}, regionName = $regionName" }
				loritta.newSuspendedTransaction {
					serverConfig.localeId = "en-us"
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

				val profile = serverConfig.getUserDataIfExistsAsync(event.guild.idLong)

				if (profile != null) {
					loritta.newSuspendedTransaction {
						profile.isInGuild = true
					}
				}

				val autoroleConfig = serverConfig.getCachedOrRetreiveFromDatabase<AutoroleConfig?>(ServerConfig::autoroleConfig)
				val welcomerConfig = serverConfig.getCachedOrRetreiveFromDatabase<WelcomerConfig?>(ServerConfig::welcomerConfig)

				queueTextChannelTopicUpdates(event.guild, serverConfig)

				if (autoroleConfig != null && autoroleConfig.enabled && !autoroleConfig.giveOnlyAfterMessageWasSent && event.guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) // Está ativado?
					AutoroleModule.giveRoles(event.member, autoroleConfig)

				if (welcomerConfig != null) // Está ativado?
					WelcomeModule.handleJoin(event, serverConfig, welcomerConfig)

				val mute = loritta.newSuspendedTransaction {
					Mute.find { (Mutes.guildId eq event.guild.idLong) and (Mutes.userId eq event.member.user.idLong) }.firstOrNull()
				}

				logger.trace { "Does ${event.member} in guild ${event.guild} has a mute status? $mute" }

				if (mute != null) {
					logger.debug { "${event.member} in guild ${event.guild} has a mute! Readding roles and recreating role removal task!" }
					val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
					val muteRole = MuteCommand.getMutedRole(event.guild, loritta.localeManager.getLocaleById(serverConfig.localeId)) ?: return@launch

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

	override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
		if (DebugLog.cancelAllEvents)
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		val member = event.member
		val user = event.user

		logger.debug { "$user ($member) left server ${event.guild}" }

		// Remover thread de role removal caso o usuário tenha saido do servidor
		val job = MuteCommand.roleRemovalJobs[event.guild.id + "#" + user.id]
		logger.debug { "Stopping mute job $job due to member guild quit" }
		job?.cancel()
		MuteCommand.roleRemovalJobs.remove(event.guild.id + "#" + user.id)

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				if (event.user.id == loritta.discordConfig.discord.clientId)
					return@launch

				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong, true)

				val profile = serverConfig.getUserDataIfExistsAsync(event.user.idLong)

				if (profile != null) {
					loritta.newSuspendedTransaction {
						profile.isInGuild = false
					}
				}

				queueTextChannelTopicUpdates(event.guild, serverConfig)

				val welcomerConfig = serverConfig.getCachedOrRetreiveFromDatabase<WelcomerConfig?>(ServerConfig::welcomerConfig)

				if (welcomerConfig != null)
					WelcomeModule.handleLeave(event, serverConfig, welcomerConfig)

				if (member != null)
					loritta.pluginManager.plugins.filterIsInstance(DiscordPlugin::class.java).flatMap {
						it.onGuildMemberLeaveListeners
					}.forEach {
						it.invoke(member, event.guild, serverConfig)
					}
			} catch (e: Exception) {
				logger.error("[${event.guild.name}] Ao sair do servidor ${event.user.name}", e)
			}
		}
	}

	override fun onGuildReady(event: GuildReadyEvent) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
			loritta.guildSetupQueue.addToSetupQueue(event.guild)
		}
	}
}