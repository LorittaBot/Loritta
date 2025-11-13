package net.perfectdreams.loritta.morenitta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.datetime.Clock
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.events.channel.ChannelCreateEvent
import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteCreateEvent
import net.dv8tion.jda.api.events.guild.invite.GuildInviteDeleteEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberRemoveEvent
import net.dv8tion.jda.api.events.http.HttpRequestEvent
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.DonationKeys
import net.perfectdreams.loritta.cinnamon.pudding.tables.Mutes
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.*
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.MemberCounterChannelConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.MiscellaneousConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationPunishmentMessagesConfig
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.WarnActions
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.vanilla.administration.MuteCommand
import net.perfectdreams.loritta.morenitta.dao.Mute
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.servers.Giveaway
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.AutoroleConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.MemberCounterChannelConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.StarboardConfig
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.WelcomerConfig
import net.perfectdreams.loritta.morenitta.modules.AutoroleModule
import net.perfectdreams.loritta.morenitta.modules.InviteLinkModule
import net.perfectdreams.loritta.morenitta.utils.debug.DebugLog
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import okio.Buffer
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.update
import java.util.concurrent.TimeUnit
import kotlin.collections.set

class DiscordListener(internal val loritta: LorittaBot) : ListenerAdapter() {
	companion object {
		// You can update a channel 2 times every 10 minutes
		// https://cdn.discordapp.com/attachments/681830234168754226/716341063912128636/unknown.png
		private const val MEMBER_COUNTER_COOLDOWN = 300_000L // 5 minutes in ms

		// We're using this mutex to avoid possible race conditions
		val jobMutex = Mutex()

		val memberCounterLastUpdate = Caffeine.newBuilder()
			.expireAfterWrite(15L, TimeUnit.MINUTES)
			.build<Long, Long>()
			.asMap()

		val memberCounterPendingJobs = Caffeine.newBuilder()
			.expireAfterWrite(3, TimeUnit.MINUTES)
			.build<Long, Job>()
			.asMap()

		/**
		 * Stores the member counter executing update mutexes, used when a topic update is being executed.
		 */
		val memberCounterExecutingUpdatesMutexes = Caffeine.newBuilder()
			.expireAfterWrite(15L, TimeUnit.MINUTES)
			.build<Long, Mutex>()
			.asMap()

		private val logger by HarmonyLoggerFactory.logger {}
		private val requestLogger by HarmonyLoggerFactory.logger("requests")

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
				memberCounterConfig != null && guild.selfMember.hasPermission(channel, Permission.MANAGE_CHANNEL) && memberCounterConfig.topic.contains("{counter}")
			}

			val channelsThatWillBeChecked = validChannels.take(ServerPremiumPlans.getPlanFromValue(activeDonationValues).memberCounterCount)

			for (textChannel in channelsThatWillBeChecked)
				GlobalScope.launch(loritta.coroutineDispatcher) {
					queueTextChannelTopicUpdate(loritta, guild, serverConfig, textChannel)
				}
		}

		private suspend fun queueTextChannelTopicUpdate(
			loritta: LorittaBot,
			guild: Guild,
			serverConfig: ServerConfig,
			textChannel: TextChannel
		) {
			jobMutex.withLock {
				if (!guild.selfMember.hasPermission(textChannel, Permission.MANAGE_CHANNEL))
					return

				val memberCountConfig = loritta.newSuspendedTransaction {
					MemberCounterChannelConfig.find {
						MemberCounterChannelConfigs.channelId eq textChannel.idLong
					}.firstOrNull()
				} ?: return

				memberCounterPendingJobs[textChannel.idLong]?.cancel()

				val job = GlobalScope.launch(loritta.coroutineDispatcher) {
					val pendingMutex = memberCounterExecutingUpdatesMutexes
						.getOrPut(textChannel.idLong) { Mutex() }

					val diff = System.currentTimeMillis() - (memberCounterLastUpdate[textChannel.idLong] ?: 0)

					pendingMutex.withLock {
						delayForTextChannelUpdateCooldown(textChannel, diff)
						updateTextChannelTopic(loritta, guild, serverConfig, textChannel, memberCountConfig)
					}
				}

				memberCounterPendingJobs[textChannel.idLong] = job

				job.invokeOnCompletion { memberCounterPendingJobs.remove(textChannel.idLong, job) }
			}
		}

		private suspend fun delayForTextChannelUpdateCooldown(textChannel: TextChannel, diff: Long) {
			if (MEMBER_COUNTER_COOLDOWN > diff) { // We are also going to offset the update for about ~5m, since we can only update a channel every 5m!
				logger.info { "Waiting ${diff}ms until the next $textChannel topic update..." }

				delay(MEMBER_COUNTER_COOLDOWN - diff)
			}
		}

		private suspend fun updateTextChannelTopic(loritta: LorittaBot, guild: Guild, serverConfig: ServerConfig, textChannel: TextChannel, memberCounterConfig: MemberCounterChannelConfig) {
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

		private val FUNNY_FIRST_EMOJIS = listOf(
			Emotes.LoriCoffee,
			Emotes.LoriHappy,
			Emotes.LoriSmile,
			Emotes.LoriSunglasses,
			Emotes.LoriUwU,
			Emotes.LoriWow,
			Emotes.LoriStonks,
			Emotes.LoriKiss,
			Emotes.LoriLick,
			Emotes.LoriFlushed
		)
	}

	override fun onHttpRequest(event: HttpRequestEvent) {
		val copy = event.requestRaw?.newBuilder()?.build()

		val body = copy?.body
		val originalMediaType = body?.contentType()
		val mediaType = "${originalMediaType?.type}/${originalMediaType?.subtype}"

		if (mediaType == "application/json") {
			// We will only write the content if the input is "application/json"
			//
			// Because if we write every body, this also includes images... And that causes Humongous Allocations (a lot of memory used)! And that's bad!!
			val buffer = Buffer()
			copy?.body?.writeTo(buffer)

			val input = buffer.readUtf8()
			val length = body?.contentLength()

			requestLogger.info { "${event.route.method.name} ${event.route.compiledRoute} Media Type: $mediaType; Content Length: $length; -> ${event.response?.code}; Body: $input" }
		} else if (originalMediaType != null) {
			requestLogger.info { "${event.route.method.name} ${event.route.compiledRoute} Media Type: $mediaType; -> ${event.response?.code}" }
		} else {
			requestLogger.info { "${event.route.method.name} ${event.route.compiledRoute} -> ${event.response?.code}" }
		}
	}

	override fun onGuildInviteCreate(event: GuildInviteCreateEvent) {
		InviteLinkModule.cachedInviteLinks.remove(event.guild.idLong)
	}

	override fun onGuildInviteDelete(event: GuildInviteDeleteEvent) {
		InviteLinkModule.cachedInviteLinks.remove(event.guild.idLong)
	}

	override fun onGenericMessageReaction(e: GenericMessageReactionEvent) {
		val user = e.user ?: return

		if (user.isBot) // Se uma mensagem de um bot, ignore a mensagem!
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
							logger.error(e) { "Erro ao tentar processar onReactionAdd" }
						}
					}
				}

				if (user.idLong == functions.originalAuthor && (functions.onReactionAddByAuthor != null || functions.onReactionByAuthor != null)) {
					GlobalScope.launch(loritta.coroutineDispatcher) {
						try {
							functions.onReactionByAuthor?.invoke(e)
							functions.onReactionAddByAuthor?.invoke(e)
						} catch (e: Exception) {
							logger.error(e) { "Erro ao tentar processar onReactionAddByAuthor" }
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
							logger.error(e) { "Erro ao tentar processar onReactionRemove" }
						}
					}
				}

				if (user.idLong == functions.originalAuthor && (functions.onReactionRemoveByAuthor != null || functions.onReactionByAuthor != null)) {
					GlobalScope.launch(loritta.coroutineDispatcher) {
						try {
							functions.onReactionByAuthor?.invoke(e)
							functions.onReactionRemoveByAuthor?.invoke(e)
						} catch (e: Exception) {
							logger.error(e) { "Erro ao tentar processar onReactionRemoveByAuthor" }
						}
					}
				}
			}
		}

		// TODO: Stop using GlobalScope
		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				// Starboard
				val config = loritta.getOrCreateServerConfig(e.guild.idLong, true)
				val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(config.localeId)
				val starboardConfig = config.getCachedOrRetreiveFromDatabase<StarboardConfig?>(loritta, ServerConfig::starboardConfig)

				if (starboardConfig != null && starboardConfig.enabled) {
					loritta.starboardModule.handleStarboardReaction(i18nContext, e, starboardConfig)
				}
			} catch (exception: Exception) {
				logger.error(exception) { "[${e.guild.name}] Starboard ${e.member?.user?.name}" }
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
					loritta.giveawayManager.cancelGiveaway(it, true, true)
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
	}

	override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
		// Remove because maybe it is present in the set
		MuteCommand.notInTheServerUserIds.remove(event.user.idLong)

		if (DebugLog.cancelAllEvents)
			return

		logger.debug { "${event.member} joined server ${event.guild}" }

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong, true)
				val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)
				val profile = serverConfig.getUserDataIfExistsAsync(loritta, event.guild.idLong)

				if (profile != null) {
					loritta.newSuspendedTransaction {
						profile.isInGuild = true
					}
				}

				val autoroleConfig = serverConfig.getCachedOrRetreiveFromDatabase<AutoroleConfig?>(loritta, ServerConfig::autoroleConfig)
				val welcomerConfig = serverConfig.getCachedOrRetreiveFromDatabase<WelcomerConfig?>(loritta, ServerConfig::welcomerConfig)

				queueTextChannelTopicUpdates(loritta, event.guild, serverConfig)

				if (autoroleConfig != null && autoroleConfig.enabled && !autoroleConfig.giveOnlyAfterMessageWasSent && event.guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) // Está ativado?
					AutoroleModule.giveRoles(event.member, autoroleConfig)

				if (welcomerConfig != null) // Está ativado?
					loritta.welcomeModule.handleJoin(event, serverConfig, i18nContext, welcomerConfig)

				val mute = loritta.newSuspendedTransaction {
					Mute.find { (Mutes.guildId eq event.guild.idLong) and (Mutes.userId eq event.member.user.idLong) }.firstOrNull()
				}

				logger.trace { "Does ${event.member} in guild ${event.guild} has a mute status? $mute" }

				if (mute != null) {
					logger.debug { "${event.member} in guild ${event.guild} has a mute! Recreating timeout updater task!" }
					val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
					MuteCommand.spawnTimeOutUpdaterThread(loritta, event.guild, locale, i18nContext, event.user, mute)
				}
			} catch (e: Exception) {
				logger.error(e) { "[${event.guild.name}] Ao entrar no servidor ${event.user.name}" }
			}
		}
	}

	override fun onGuildMemberRemove(event: GuildMemberRemoveEvent) {
		if (DebugLog.cancelAllEvents)
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
				if (event.user.id == loritta.config.loritta.discord.applicationId.toString())
					return@launch

				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong, true)
				val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)
				val profile = serverConfig.getUserDataIfExistsAsync(loritta, event.user.idLong)

				if (profile != null) {
					loritta.newSuspendedTransaction {
						profile.isInGuild = false
					}
				}

				queueTextChannelTopicUpdates(loritta, event.guild, serverConfig)

				val welcomerConfig = serverConfig.getCachedOrRetreiveFromDatabase<WelcomerConfig?>(loritta, ServerConfig::welcomerConfig)

				if (welcomerConfig != null)
					loritta.welcomeModule.handleLeave(event, serverConfig, i18nContext, welcomerConfig)
			} catch (e: Exception) {
				logger.error(e) { "[${event.guild.name}] Ao sair do servidor ${event.user.name}" }
			}
		}
	}

	override fun onReady(event: ReadyEvent) {
		// This is called when the shard is ready and all guilds are ready to be used
		GlobalScope.launch(loritta.coroutineDispatcher) {
			val start = Clock.System.now()

			// Get all guild IDs
			val guildIds = event.jda.guildCache.map { it.idLong }

			// No need to process if the guild map is empty (this should never happen, but who knows right)
			if (guildIds.isEmpty())
				return@launch

			logger.info { "Querying configs of ${guildIds.size} guilds to start the setup process for shard ${event.jda.shardInfo.shardId}" }

			// Everything is good? Great! Let's prepare all guilds then!
			val (serverConfigs, guildMutes, allGuildActiveGiveaways) = loritta.newSuspendedTransaction {
				// The reason we chunk the query in multiple queries is due to this issue:
				// https://github.com/LorittaBot/Loritta/issues/2343
				// https://stackoverflow.com/questions/49274390/postgresql-and-hibernate-java-io-ioexception-tried-to-send-an-out-of-range-inte
				// Since PostgreSQL JDBC 42.3.7, the max parameter size is 65_535 parameters. This issue only affects "inList" queries!
				val serverConfigs = guildIds.chunked(65_535).flatMap {
					ServerConfig.find {
						ServerConfigs.id inList it
					}.toList()
				}

				logger.info { "Preparing ${guildIds.size} guilds with ${serverConfigs.size} server configs for shard ${event.jda.shardInfo.shardId}" }

				// We also chunk this too
				val guildMutes = guildIds.chunked(65_535).flatMap {
					Mute.find {
						(Mutes.isTemporary eq true) and (Mutes.guildId inList it)
					}.toList()
				}

				// We also chunk this too²
				val allGuildActiveGiveaways = guildIds.chunked(65_535).flatMap {
					Giveaway.find {
						(Giveaways.guildId inList it) and (Giveaways.finished eq false)
					}.toList()
				}

				Triple(serverConfigs, guildMutes, allGuildActiveGiveaways)
			}

			// The reason we process it outside of the previous transaction is to avoid any nested transaction calls causing a deadlock
			for (mute in guildMutes) {
				val guild = event.jda.getGuildById(mute.guildId)

				if (guild == null) {
					logger.warn { "Guild ${mute.guildId} does not exist on shard ${event.jda.shardInfo.shardId}, skipping mute task setup..." }
					continue
				}

				val serverConfig = serverConfigs.firstOrNull { it.guildId == mute.guildId }
				if (serverConfig == null) {
					logger.warn { "Guild ${mute.guildId} does not have a server configuration on shard ${event.jda.shardInfo.shardId}, skipping mute task setup..." }
					continue
				}
				val i18nContext = loritta.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

				logger.info { "Adicionado removal thread pelo MutedUsersThread já que a guild iniciou! ~ Guild: ${mute.guildId} - User: ${mute.userId}" }
				MuteCommand.spawnTimeOutUpdaterThread(loritta, guild.idLong, loritta.localeManager.getLocaleById(serverConfig.localeId), i18nContext, mute.userId, mute)
			}


			allGuildActiveGiveaways.forEach {
				try {
					if (loritta.giveawayManager.giveawayTasks[it.id.value] == null)
						loritta.giveawayManager.createGiveawayJob(it)
				} catch (e: Exception) {
					logger.error(e) { "Error while creating giveaway ${it.id.value} job on guild ready ${it.guildId} for shard ${event.jda.shardInfo.shardId}" }
				}
			}

			logger.info { "Done! ${guildIds.size} guilds were set up for shard ${event.jda.shardInfo.shardId}! Let's roll!! Took ${Clock.System.now() - start}ms" }
		}
	}

	override fun onChannelCreate(event: ChannelCreateEvent) {
		// This should only be sent in a guild text channel
		val channel = event.channel
		if (channel is GuildMessageChannel) {
			GlobalScope.launch(loritta.coroutineDispatcher) {
				val miscellaneousConfig = loritta.transaction {
					MiscellaneousConfigs.innerJoin(ServerConfigs).selectFirstOrNull {
						ServerConfigs.id eq event.guild.idLong
					}
				} ?: return@launch

				if (!miscellaneousConfig[MiscellaneousConfigs.enableQuirky])
					return@launch

				channel.sendMessage(
					MessageCreate {
						content = "First! ${FUNNY_FIRST_EMOJIS.random()}"
					}
				).await()
			}
		}
	}
}