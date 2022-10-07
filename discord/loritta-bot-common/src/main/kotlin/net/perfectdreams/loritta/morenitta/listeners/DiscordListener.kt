package net.perfectdreams.loritta.morenitta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.MuteCommand
import net.perfectdreams.loritta.morenitta.dao.Mute
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.modules.AutoroleModule
import net.perfectdreams.loritta.morenitta.tables.DonationKeys
import net.perfectdreams.loritta.morenitta.tables.GuildProfiles
import net.perfectdreams.loritta.morenitta.tables.Mutes
import net.perfectdreams.loritta.morenitta.utils.debug.DebugLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import dev.kord.common.entity.Permission
import net.perfectdreams.loritta.deviousfun.entities.Guild
import net.perfectdreams.loritta.morenitta.dao.servers.Giveaway
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.AutoroleConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.MemberCounterChannelConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.WelcomerConfig
import net.perfectdreams.loritta.morenitta.tables.servers.CustomGuildCommands
import net.perfectdreams.loritta.morenitta.tables.servers.Giveaways
import net.perfectdreams.loritta.morenitta.tables.servers.ServerRolePermissions
import net.perfectdreams.loritta.morenitta.tables.servers.moduleconfigs.MemberCounterChannelConfigs
import net.perfectdreams.loritta.morenitta.tables.servers.moduleconfigs.ModerationPunishmentMessagesConfig
import net.perfectdreams.loritta.morenitta.tables.servers.moduleconfigs.WarnActions
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.deviousfun.entities.Channel
import net.perfectdreams.loritta.deviousfun.events.guild.GuildJoinEvent
import net.perfectdreams.loritta.deviousfun.events.guild.GuildLeaveEvent
import net.perfectdreams.loritta.deviousfun.events.guild.GuildReadyEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberJoinEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberRemoveEvent
import net.perfectdreams.loritta.deviousfun.events.message.react.GenericMessageReactionEvent
import net.perfectdreams.loritta.deviousfun.events.message.react.MessageReactionAddEvent
import net.perfectdreams.loritta.deviousfun.events.message.react.MessageReactionRemoveEvent
import net.perfectdreams.loritta.deviousfun.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import kotlin.collections.set

class DiscordListener(internal val loritta: LorittaBot) : ListenerAdapter() {
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

		suspend fun queueTextChannelTopicUpdates(loritta: LorittaBot, guild: Guild, serverConfig: ServerConfig) {
			val activeDonationValues = loritta.getOrCreateServerConfig(guild.idLong).getActiveDonationKeysValue(loritta)

			logger.debug { "Creating text channel topic updates in $guild for ${guild.textChannels.size} channels! Donation key value is $activeDonationValues" }

			val memberCountConfigs = loritta.newSuspendedTransaction {
				MemberCounterChannelConfig.find {
					MemberCounterChannelConfigs.channelId inList guild.channels.map { it.idLong }
				}.toList()
			}

			val validChannels = guild.textChannels.filter { channel ->
				val memberCounterConfig = memberCountConfigs.firstOrNull { it.channelId == channel.idLong }
				memberCounterConfig != null && guild.selfMemberHasPermission(channel, Permission.ManageChannels) && memberCounterConfig.topic.contains("{counter}")
			}

			val channelsThatWillBeChecked = validChannels.take(ServerPremiumPlans.getPlanFromValue(activeDonationValues).memberCounterCount)

			for (textChannel in channelsThatWillBeChecked)
				GlobalScope.launch(loritta.coroutineDispatcher) {
					queueTextChannelTopicUpdate(loritta, guild, serverConfig, textChannel)
				}
		}

		private suspend fun queueTextChannelTopicUpdate(loritta: LorittaBot, guild: Guild, serverConfig: ServerConfig, textChannel: Channel) {
			if (!guild.selfMemberHasPermission(textChannel, Permission.ManageChannels))
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

						updateTextChannelTopic(loritta, guild, serverConfig, textChannel, memberCountConfig)
					}
				}
				return
			}

			memberCounterExecutingUpdateMutex.withLock {
				delayForTextChannelUpdateCooldown(textChannel, diff)

				updateTextChannelTopic(loritta, guild, serverConfig, textChannel, memberCountConfig)
			}
		}

		private suspend fun delayForTextChannelUpdateCooldown(textChannel: Channel, diff: Long) {
			if (MEMBER_COUNTER_COOLDOWN > diff) { // We are also going to offset the update for about ~5m, since we can only update a channel every 5m!
				logger.info { "Waiting ${diff}ms until the next $textChannel topic update..." }

				delay(MEMBER_COUNTER_COOLDOWN - diff)
			}
		}

		private suspend fun updateTextChannelTopic(loritta: LorittaBot, guild: Guild, serverConfig: ServerConfig, textChannel: Channel, memberCounterConfig: MemberCounterChannelConfig) {
			val formattedTopic = memberCounterConfig.getFormattedTopic(guild)

			val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
			logger.info  { "Updating text channel $textChannel topic in $guild!" }
			logger.trace { "Member Counter Theme = ${memberCounterConfig.theme}"}
			logger.trace { "Member Counter Padding = ${memberCounterConfig.padding}"}
			logger.trace { "Formatted Topic = $formattedTopic" }

			// The reason we use ".await()" is so we can track when the request is successfully sent!
			// And, if the request is rate limited, it will take more time to be processed, which is perfect for us!
			textChannel.modifyTextChannel {
				topic = formattedTopic
				reason = locale["loritta.modules.counter.auditLogReason"]
			}
			memberCounterLastUpdate[textChannel.idLong] = System.currentTimeMillis()
		}
	}

	/* override fun onHttpRequest(event: HttpRequestEvent) {
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
	} */

	override fun onGenericMessageReaction(event: GenericMessageReactionEvent) {
		val user = event.user ?: return

		if (user.isBot) // Se uma mensagem de um bot, ignore a mensagem!
			return

		if (DebugLog.cancelAllEvents)
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		if (loritta.messageInteractionCache.containsKey(event.messageIdLong)) {
			val functions = loritta.messageInteractionCache[event.messageIdLong]!!

			if (event is MessageReactionAddEvent) {
				if (functions.onReactionAdd != null) {
					GlobalScope.launch(loritta.coroutineDispatcher) {
						try {
							functions.onReactionAdd!!.invoke(event)
						} catch (e: Exception) {
							logger.error("Erro ao tentar processar onReactionAdd", e)
						}
					}
				}

				if (user.idLong == functions.originalAuthor && (functions.onReactionAddByAuthor != null || functions.onReactionByAuthor != null)) {
					GlobalScope.launch(loritta.coroutineDispatcher) {
						try {
							functions.onReactionByAuthor?.invoke(event)
							functions.onReactionAddByAuthor?.invoke(event)
						} catch (e: Exception) {
							logger.error("Erro ao tentar processar onReactionAddByAuthor", e)
						}
					}
				}
			}

			if (event is MessageReactionRemoveEvent) {
				if (user.idLong == functions.originalAuthor && (functions.onReactionAddByAuthor != null || functions.onReactionByAuthor != null)) {
					GlobalScope.launch(loritta.coroutineDispatcher) {
						try {
							functions.onReactionByAuthor?.invoke(event)
						} catch (e: Exception) {
							logger.error("Erro ao tentar processar onReactionByAuthor", e)
						}
					}
				}
			}
		}
	}

	override fun onGuildLeave(e: GuildLeaveEvent) {
		logger.info { "Someone removed me @ ${e.guildIdLong}! :(" }

		loritta.cachedServerConfigs.invalidate(e.guildIdLong)

		// Remover threads de role removal caso a Loritta tenha saido do servidor
		val toRemove = mutableListOf<String>()
		MuteCommand.roleRemovalJobs.forEach { key, value ->
			if (key.startsWith(e.guildId)) {
				logger.debug { "Stopping mute job $value @ ${e.guildIdLong} because they removed me!" }
				value.cancel()
				toRemove.add(key)
			}
		}
		toRemove.forEach { MuteCommand.roleRemovalJobs.remove(it) }

		logger.debug { "Deleting all ${e.guildIdLong} related stuff..." }

		GlobalScope.launch(loritta.coroutineDispatcher) {
			loritta.newSuspendedTransaction {
				logger.trace { "Deleting all ${e.guildIdLong} profiles..."}

				// Deletar todos os perfis do servidor
				GuildProfiles.deleteWhere {
					GuildProfiles.guildId eq e.guildIdLong
				}

				// Deletar configurações
				logger.trace { "Deleting all ${e.guildIdLong} configurations..." }
				val serverConfig = ServerConfig.findById(e.guildIdLong)

				logger.trace { "Removing all donation keys references about ${e.guildIdLong}..." }
				if (serverConfig != null) {
					DonationKeys.update({ DonationKeys.activeIn eq serverConfig.id }) {
						it[activeIn] = null
					}

					logger.trace { "Deleting all ${e.guildIdLong} role perms..." }

					ServerRolePermissions.deleteWhere {
						ServerRolePermissions.guild eq serverConfig.id
					}

					logger.trace { "Deleting all ${e.guildIdLong} custom commands..." }

					CustomGuildCommands.deleteWhere {
						CustomGuildCommands.guild eq serverConfig.id
					}

					val moderationConfig = serverConfig?.moderationConfig
					logger.trace { "Deleting all ${e.guildIdLong} warn actions..." }
					if (moderationConfig != null)
						WarnActions.deleteWhere {
							WarnActions.config eq moderationConfig.id
						}

					logger.trace { "Deleting all ${e.guildIdLong} member counters..." }

					MemberCounterChannelConfigs.deleteWhere {
						MemberCounterChannelConfigs.guild eq serverConfig.id
					}

					logger.trace { "Deleting all ${e.guildIdLong} moderation messages counters..." }

					ModerationPunishmentMessagesConfig.deleteWhere {
						ModerationPunishmentMessagesConfig.guild eq serverConfig.id
					}
				}

				logger.trace { "Deleting ${e.guildIdLong} config..." }
				serverConfig?.delete()

				logger.trace { "Deleting all ${e.guildIdLong}'s giveaways..." }
				val allGiveaways = Giveaway.find {
					Giveaways.guildId eq e.guildIdLong
				}

				logger.trace { "${e.guildIdLong} has ${allGiveaways.count()} giveaways that will be cancelled and deleted!" }

				allGiveaways.forEach {
					loritta.giveawayManager.cancelGiveaway(it, true, true)
				}

				Mutes.deleteWhere {
					Mutes.guildId eq e.guildIdLong
				}

				logger.trace { "Done! Everything related to ${e.guildIdLong} was deleted!" }
			}
		}
	}

	override fun onGuildJoin(event: GuildJoinEvent) {
		logger.info { "Someone added me @ ${event.guildIdLong}! :)" }
	}

	override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
		// Remove because maybe it is present in the set
		MuteCommand.notInTheServerUserIds.remove(event.user.idLong)

		if (DebugLog.cancelAllEvents)
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		logger.debug { "${event.member} joined server ${event.guild}" }

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong, true)

				val profile = serverConfig.getUserDataIfExistsAsync(loritta, event.guild.idLong)

				if (profile != null) {
					loritta.newSuspendedTransaction {
						profile.isInGuild = true
					}
				}

				val autoroleConfig = serverConfig.getCachedOrRetreiveFromDatabase<AutoroleConfig?>(loritta, ServerConfig::autoroleConfig)
				val welcomerConfig = serverConfig.getCachedOrRetreiveFromDatabase<WelcomerConfig?>(loritta, ServerConfig::welcomerConfig)

				queueTextChannelTopicUpdates(loritta, event.guild, serverConfig)

				if (autoroleConfig != null && autoroleConfig.enabled && !autoroleConfig.giveOnlyAfterMessageWasSent && event.guild.selfMemberHasPermission(Permission.ManageRoles)) // Está ativado?
					AutoroleModule.giveRoles(event.member, autoroleConfig)

				if (welcomerConfig != null) // Está ativado?
					loritta.welcomeModule.handleJoin(event, serverConfig, welcomerConfig)

				val mute = loritta.newSuspendedTransaction {
					Mute.find { (Mutes.guildId eq event.guild.idLong) and (Mutes.userId eq event.member.user.idLong) }.firstOrNull()
				}

				logger.trace { "Does ${event.member} in guild ${event.guild} has a mute status? $mute" }

				if (mute != null) {
					logger.debug { "${event.member} in guild ${event.guild} has a mute! Readding roles and recreating role removal task!" }
					val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
					val muteRole = MuteCommand.getMutedRole(loritta, event.guild, loritta.localeManager.getLocaleById(serverConfig.localeId)) ?: return@launch

					event.guild.addRoleToMember(event.member, muteRole)

					if (mute.isTemporary)
						MuteCommand.spawnRoleRemovalThread(loritta, event.guild, locale, event.user, mute.expiresAt!!)
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

		val user = event.user

		logger.debug { "$user left server ${event.guild}" }

		// Remover thread de role removal caso o usuário tenha saido do servidor
		val job = MuteCommand.roleRemovalJobs[event.guild.id + "#" + user.id]
		logger.debug { "Stopping mute job $job due to member guild quit" }
		job?.cancel()
		MuteCommand.roleRemovalJobs.remove(event.guild.id + "#" + user.id)

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				if (event.user.id == loritta.config.loritta.discord.applicationId.toString())
					return@launch

				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong, true)

				val profile = serverConfig.getUserDataIfExistsAsync(loritta, event.user.idLong)

				if (profile != null) {
					loritta.newSuspendedTransaction {
						profile.isInGuild = false
					}
				}

				queueTextChannelTopicUpdates(loritta, event.guild, serverConfig)

				val welcomerConfig = serverConfig.getCachedOrRetreiveFromDatabase<WelcomerConfig?>(loritta, ServerConfig::welcomerConfig)

				if (welcomerConfig != null)
					loritta.welcomeModule.handleLeave(event, serverConfig, welcomerConfig)
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