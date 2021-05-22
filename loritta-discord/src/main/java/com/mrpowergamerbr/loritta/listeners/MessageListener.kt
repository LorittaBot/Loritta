package com.mrpowergamerbr.loritta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.AutoroleModule
import com.mrpowergamerbr.loritta.modules.Modules
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.eventlog.EventLog
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.MessageBulkDeleteEvent
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.dao.servers.moduleconfigs.AutoroleConfig
import net.perfectdreams.loritta.platform.discord.legacy.entities.jda.JDAUser
import net.perfectdreams.loritta.platform.discord.legacy.plugin.DiscordPlugin
import net.perfectdreams.loritta.platform.discord.legacy.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.utils.Emotes
import org.apache.commons.text.similarity.LevenshteinDistance
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class MessageListener(val loritta: Loritta) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
		// Our blacklist of messages (messages that we'll ignore on command execution)
		private val unavailableMessages = Collections.newSetFromMap(Caffeine.newBuilder().expireAfterWrite(15L, TimeUnit.MINUTES).build<Long, Boolean>().asMap())

		val MESSAGE_RECEIVED_MODULES = mutableListOf(
				Modules.AUTOMOD,
				Modules.INVITE_LINK,
				Modules.EXPERIENCE,
				Modules.AFK,
				Modules.BOM_DIA_E_CIA
		)

		val MESSAGE_EDITED_MODULES = mutableListOf(
				Modules.INVITE_LINK,
				Modules.AUTOMOD
		)
	}

	override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
		if (loritta.discordConfig.discord.disallowBots && !loritta.discordConfig.discord.botWhitelist.contains(event.author.idLong) && event.author.isBot) // Se uma mensagem de um bot, ignore a mensagem!
			return

		if (DebugLog.cancelAllEvents)
			return

		if (event.message.type != MessageType.DEFAULT && event.message.type != MessageType.INLINE_REPLY) // Existem vários tipos de mensagens no Discord, mas apenas estamos interessados nas mensagens padrões de texto
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		loritta.launchMessageJob(event) {
			try {
				val member = event.member
				if (member == null) { // This may seem dumb, but it works!
					logger.warn { "${event.author} saiu do servidor ${event.guild.id} antes de eu poder processar a mensagem"}
					return@launchMessageJob
				}

				val enableProfiling = loritta.config.isOwner(member.idLong)

				var start = System.nanoTime()

				val serverConfigJob = loritta.getOrCreateServerConfigDeferred(event.guild.idLong, true)
						.logOnCompletion(enableProfiling) { "Loading Server Config took {time}ns for ${event.author.idLong}" }
				val lorittaProfileJob = loritta.getLorittaProfileDeferred(event.author.idLong)
						.logOnCompletion(enableProfiling) { "Loading user's profile took {time}ns for ${event.author.idLong}" }

				val serverConfig = serverConfigJob.await()

				val autoroleConfigJob = serverConfig.getCachedOrRetreiveFromDatabaseDeferred<AutoroleConfig?>(loritta, ServerConfig::autoroleConfig)
						.logOnCompletion(enableProfiling) { "Loading Server Config's autorole took {time}ns for ${event.author.idLong}" }

				val lorittaProfile = lorittaProfileJob.await()
				val autoroleConfig = autoroleConfigJob.await()

				start = System.nanoTime()

				logIfEnabled(enableProfiling) { "Migration Checks took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				val currentLocale = loritta.newSuspendedTransaction {
					(lorittaProfile?.settings?.language ?: serverConfig.localeId)
				}
				val locale = loritta.localeManager.getLocaleById(currentLocale)
				logIfEnabled(enableProfiling) { "Loading ${locale.id} locale took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				// We use "loadMemberRolesLorittaPermissions(...)" to avoid unnecessary retrievals later on, because we recheck the role permission later
				val rolesLorittaPermissions = serverConfig.getOrLoadGuildRolesLorittaPermissions(event.guild)
				logIfEnabled(enableProfiling) { "Loading Loritta's role permissions in ${event.guild.idLong} took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				val memberLorittaPermissions = LorittaUser.convertRolePermissionsMapToMemberPermissionList(member, rolesLorittaPermissions)
				logIfEnabled(enableProfiling) { "Converting Loritta's role permissions to member permissions in ${event.guild.idLong} took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				val lorittaUser = GuildLorittaUser(member, memberLorittaPermissions, lorittaProfile)
				logIfEnabled(enableProfiling) { "Wrapping $member and $lorittaProfile in a GuildLorittaUser took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				if (lorittaProfile != null && lorittaProfile.isAfk) {
					loritta.newSuspendedTransaction {
						lorittaProfile.isAfk = false
						lorittaProfile.afkReason = null
					}
				}
				logIfEnabled(enableProfiling) { "Changing AFK status took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				EventLog.onMessageReceived(serverConfig, event.message)
				logIfEnabled(enableProfiling) { "Logging to EventLog took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()

				logIfEnabled(enableProfiling) { "Checking user mention took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				if (isMentioningOnlyMe(event.message.contentRaw)) {
					if (chance(25.0))
						event.message.addReaction("smol_lori_putassa_ping:397748526362132483").queue()

					var response = locale["commands.mention.response", member.asMention, serverConfig.commandPrefix]

					if (lorittaUser.hasPermission(LorittaPermission.IGNORE_COMMANDS)) {
						// Usuário não pode usar comandos

						// Qual é o cargo que não permite utilizar os meus comandos?
						val roles = member.roles.toMutableList()

						roles.add(member.guild.publicRole)
						roles.sortByDescending { it.position }

						var ignoringCommandsRole: Role? = null
						for (role in roles) {
							val permissions = rolesLorittaPermissions[role.idLong] ?: continue
							if (permissions.contains(LorittaPermission.IGNORE_COMMANDS)) {
								ignoringCommandsRole = role
								break
							}
						}

						if (ignoringCommandsRole == event.guild.publicRole)
							response = locale["commands.mention.responseEveryoneBlocked", event.message.author.asMention, serverConfig.commandPrefix]
						else
							response = locale["commands.mention.responseRoleBlocked", event.message.author.asMention, serverConfig.commandPrefix, "`${ignoringCommandsRole?.name}`"]
					} else {
						if (serverConfig.blacklistedChannels.contains(event.channel.idLong) && !lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
							// Vamos pegar um canal que seja possível usar comandos
							val useCommandsIn = event.guild.textChannels.firstOrNull { !serverConfig.blacklistedChannels.contains(it.idLong) && it.canTalk(member) }

							response = if (useCommandsIn != null) {
								// Canal não bloqueado!
								locale["commands.mention.responseBlocked", event.message.author.asMention, serverConfig.commandPrefix, useCommandsIn.asMention]
							} else {
								// Nenhum canal disponível...
								locale["commands.mention.responseBlockedNoChannels", event.message.author.asMention, serverConfig.commandPrefix]
							}
						}
					}

					val responseBuilder = MessageBuilder()
						.setAllowedMentions(listOf(Message.MentionType.USER, Message.MentionType.CHANNEL))
						.setContent("<:loritta:331179879582269451> **|** $response")

					if (event.channel.canTalk()) {
						event.channel.sendMessage(responseBuilder.build()).queue()
					} else {
						event.author.openPrivateChannel().queue {
							it.sendMessage(responseBuilder.build()).queue()
						}
					}
				}
				logIfEnabled(enableProfiling) { "Checking self mention for help took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				val lorittaMessageEvent = LorittaMessageEvent(
						event.author,
						member,
						event.message,
						event.messageId,
						event.guild,
						event.channel,
						event.channel,
						serverConfig,
						locale,
						lorittaUser
				)
				logIfEnabled(enableProfiling) { "Creating a LorittaMessageEvent took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()

				if (autoroleConfig != null && autoroleConfig.enabled && autoroleConfig.giveOnlyAfterMessageWasSent && event.guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) // Está ativado?
					AutoroleModule.giveRoles(member, autoroleConfig)

				logIfEnabled(enableProfiling) { "Giving auto role on message took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				for (module in (MESSAGE_RECEIVED_MODULES + loritta.pluginManager.plugins.filterIsInstance<DiscordPlugin>().flatMap { it.messageReceivedModules } + loritta.pluginManager.plugins.filterIsInstance<LorittaDiscordPlugin>().flatMap { it.messageReceivedModules })) {
					start = System.nanoTime()
					if (module.matches(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, locale) && module.handle(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, locale))
						return@launchMessageJob
					logIfEnabled(enableProfiling) { "Executing ${module::class.simpleName} took ${System.nanoTime() - start}ns for ${event.author.idLong}" }
				}

				start = System.nanoTime()
				if (lorittaUser.hasPermission(LorittaPermission.IGNORE_COMMANDS))
					return@launchMessageJob
				logIfEnabled(enableProfiling) { "Checking for ignore permission took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				// Executar comandos
				start = System.nanoTime()
				if (checkCommandsAndDispatch(lorittaMessageEvent, serverConfig, locale, lorittaUser))
					return@launchMessageJob
				logIfEnabled(enableProfiling) { "All commands check took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				loritta.messageInteractionCache.values.toMutableList().forEach {
					if (it.onMessageReceived != null)
						it.onMessageReceived!!.invoke(lorittaMessageEvent)

					if (it.guildId == event.guild.idLong && it.channelId == event.channel.idLong) {
						if (it.onResponse != null)
							it.onResponse!!.invoke(lorittaMessageEvent)

						if (it.onResponseByAuthor != null) {
							if (it.originalAuthor == event.author.idLong)
								it.onResponseByAuthor!!.invoke(lorittaMessageEvent)
						}
					}
				}
				logIfEnabled(enableProfiling) { "Checking for interaction cache took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				if (event.channel.canTalk() && serverConfig.warnOnUnknownCommand) {
					val startsWithCommandPattern = Regex("^" + Pattern.quote(serverConfig.commandPrefix) + "[A-z0-9]+.*")

					if (event.message.contentRaw.matches(startsWithCommandPattern)) {
						val command = event.message.contentDisplay.split(" ")[0].stripCodeMarks()
								.substring(serverConfig.commandPrefix.length)

						val list = mutableListOf(
								LorittaReply(
										"${locale["commands.unknownCommand", command, "${serverConfig.commandPrefix}${locale["commands.helpCommandName"]}"]} ${Emotes.LORI_OWO}",
										"\uD83E\uDD37"
								)
						)

						val allCommandLabels = mutableListOf<String>()

						loritta.commandMap.commands.forEach {
							if (!it.onlyOwner && !serverConfig.disabledCommands.contains(it.javaClass.simpleName) && !it.hideInHelp)
								allCommandLabels.addAll(it.labels)
						}

						loritta.legacyCommandManager.commandMap.forEach {
							if (!it.onlyOwner && !serverConfig.disabledCommands.contains(it.javaClass.simpleName)) {
								allCommandLabels.add(it.label)
								allCommandLabels.addAll(it.aliases)
							}
						}

						var diff = 999
						var nearestCommand: String? = null

						for (label in allCommandLabels) {
							val _diff = LevenshteinDistance.getDefaultInstance().apply(command, label)

							if (diff > _diff) {
								nearestCommand = label
								diff = _diff
							}
						}

						if (nearestCommand != null && 6 > diff) {
							list.add(
									LorittaReply(
											prefix = Emotes.LORI_HM,
											message = locale["commands.didYouMeanCommand", serverConfig.commandPrefix + nearestCommand],
											mentionUser = false
									)
							)
						}

						event.channel.sendMessage(list.joinToString("\n") { it.build(JDAUser(event.author)) }).queue {
							it.delete().queueAfter(5000, TimeUnit.MILLISECONDS)
						}
					}
				}
				logIfEnabled(enableProfiling) { "Checking for similar commands took ${System.nanoTime() - start}ns for ${event.author.idLong}" }
			} catch (e: Exception) {
				logger.error("[${event.guild.name}] Erro ao processar mensagem de ${event.author.name} (${event.author.id} - ${event.message.contentRaw}", e)
			}
		}
	}

	override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
		// Bots não conseguem enviar mensagens para si mesmo... mas a Lori consegue e, com o "say", é possível fazer ela executar os próprios comandos
		if (loritta.discordConfig.discord.disallowBots && !loritta.discordConfig.discord.botWhitelist.contains(event.author.idLong) && event.author.isBot) // Se uma mensagem de um bot, ignore a mensagem!
			return

		if (DebugLog.cancelAllEvents)
			return

		if (event.message.type != MessageType.DEFAULT && event.message.type != MessageType.INLINE_REPLY) // Existem vários tipos de mensagens no Discord, mas apenas estamos interessados nas mensagens padrões de texto
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		loritta.launchMessageJob(event) {
			val serverConfig = loritta.getOrCreateServerConfigAsync(-1, true)
			val profile = loritta.getOrCreateLorittaProfile(event.author.idLong) // Carregar perfil do usuário
			val lorittaUser = LorittaUser(event.author, EnumSet.noneOf(LorittaPermission::class.java), profile)
			val currentLocale = loritta.newSuspendedTransaction {
				profile.settings.language ?: "default"
			}
			val locale = loritta.localeManager.getLocaleById(currentLocale)

			if (isMentioningOnlyMe(event.message.contentRaw)) {
				event.channel.sendMessage(locale["commands.commandsInDirectMessage", event.message.author.asMention, locale["commands.helpCommandName"]]).queue()
				return@launchMessageJob
			}

			val lorittaMessageEvent = LorittaMessageEvent(
					event.author,
					null,
					event.message,
					event.messageId,
					null,
					event.channel,
					null,
					serverConfig,
					locale,
					lorittaUser
			)

			// Executar comandos
			if (checkCommandsAndDispatch(lorittaMessageEvent, serverConfig, locale, lorittaUser))
				return@launchMessageJob
		}
	}

	override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
		// If message was pinned, let's add it to our "blacklist"
		if (event.message.isPinned) unavailableMessages.add(event.messageIdLong)
		// If message is in our blacklist, lets ignore the event
		if (unavailableMessages.contains(event.messageIdLong)) return

		// Checking if message was sent before 15 minutes ago (900 000ms)
		if (System.currentTimeMillis() - 900_000 >= event.message.timeCreated.toEpochSecond() * 1000) return

		if (loritta.discordConfig.discord.disallowBots && !loritta.discordConfig.discord.botWhitelist.contains(event.author.idLong) && event.author.isBot) // Se uma mensagem de um bot, ignore a mensagem!
			return

		if (DebugLog.cancelAllEvents)
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		if (event.channel.type == ChannelType.TEXT) { // Mensagens em canais de texto
			GlobalScope.launch(loritta.coroutineDispatcher) {
				val member = event.member

				if (member == null) { // This may seem dumb, but it works!
					logger.warn { "${event.author} saiu do servidor ${event.guild.id} antes de eu poder processar a mensagem" }
					return@launch
				}

				val serverConfig = loritta.getOrCreateServerConfig(event.guild.idLong, true)
				val lorittaProfile = loritta.getOrCreateLorittaProfile(event.author.idLong)
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				val lorittaUser = GuildLorittaUser(
						member,
						LorittaUser.convertRolePermissionsMapToMemberPermissionList(
								member,
								serverConfig.getOrLoadGuildRolesLorittaPermissions(event.guild)
						),
						lorittaProfile)

				EventLog.onMessageUpdate(serverConfig, locale, event.message)

				val lorittaMessageEvent = LorittaMessageEvent(
						event.author,
						event.member,
						event.message,
						event.messageId,
						event.guild,
						event.channel,
						event.channel,
						serverConfig,
						locale,
						lorittaUser
				)

				for (module in (MESSAGE_EDITED_MODULES + loritta.pluginManager.plugins.filterIsInstance<DiscordPlugin>().flatMap { it.messageEditedModules } + loritta.pluginManager.plugins.filterIsInstance<LorittaDiscordPlugin>().flatMap { it.messageEditedModules })) {
					if (module.matches(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, locale) && module.handle(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, locale))
						return@launch
				}

				if (checkCommandsAndDispatch(lorittaMessageEvent, serverConfig, locale, lorittaUser))
					return@launch
			}
		}
	}

	override fun onMessageDelete(event: MessageDeleteEvent) {
		loritta.messageInteractionCache.remove(event.messageIdLong)
	}

	override fun onMessageBulkDelete(event: MessageBulkDeleteEvent) {
		// If the messages are bulk deleted, we also need to remove them from the message interaction cache too!
		//
		// If not, this can cause interactions to be persisted, causing issues related to "Loritta never stops replying to this message"
		// because the "source" message was deleted.
		event.messageIds.forEach { loritta.messageInteractionCache.remove(it.toLong()) }
	}

	/**
	 * Checks and dispatches commands
	 *
	 * @return if a command was dispatched
	 */
	suspend fun checkCommandsAndDispatch(lorittaMessageEvent: LorittaMessageEvent, serverConfig: ServerConfig, locale: BaseLocale, lorittaUser: LorittaUser): Boolean {
		// If Loritta can't speak in the current channel, do *NOT* try to process a command! If we try to process, Loritta will have issues that she wants to talk in a channel, but she doesn't have the "canTalk()" permission!
		if (lorittaMessageEvent.channel is TextChannel && !lorittaMessageEvent.channel.canTalk())
			return false

		val author = lorittaMessageEvent.author
		val enableProfiling = loritta.config.isOwner(author.idLong)

		val rawMessage = lorittaMessageEvent.message.contentRaw

		val rawArguments = rawMessage
				.split(" ")
				.toMutableList()

		val firstLabel = rawArguments.first()
		val startsWithCommandPrefix = firstLabel.startsWith(serverConfig.commandPrefix)
		val startsWithLorittaMention = firstLabel == "<@${com.mrpowergamerbr.loritta.utils.loritta.discordConfig.discord.clientId}>" || firstLabel == "<@!${com.mrpowergamerbr.loritta.utils.loritta.discordConfig.discord.clientId}>"

		if (startsWithCommandPrefix || startsWithLorittaMention) {
			if (startsWithCommandPrefix) // If it is a command prefix, remove the prefix
				rawArguments[0] = rawArguments[0].removePrefix(serverConfig.commandPrefix)
			else if (startsWithLorittaMention) { // If it is a mention, remove the first argument (which is Loritta's mention)
				rawArguments.removeAt(0)
				if (rawArguments.isEmpty()) // If it is empty, then it means that it was only Loritta's mention, so just return false
					return false
			}

			// To fix commands like "+eval" *line break* "code code code", we are going to remove the first new line from the first argument
			if (rawArguments[0].contains("\n")) {
				val splitNewLines = rawArguments[0].split(Regex("(?=\n+)", RegexOption.MULTILINE))
				rawArguments[0] = splitNewLines[0]
						.replace("\n", "")

				rawArguments.addAll(1, splitNewLines.drop(1))
			}

			// Executar comandos
			var start = System.nanoTime()
			if (loritta.commandManager.matches(lorittaMessageEvent, rawArguments, serverConfig, locale, lorittaUser))
				return true
			logIfEnabled(enableProfiling) { "Checking for command manager commands took ${System.nanoTime() - start}ns for ${author.idLong}" }

			start = System.nanoTime()
			if (loritta.commandMap.dispatch(lorittaMessageEvent, rawArguments, serverConfig, locale, lorittaUser))
				return true
			logIfEnabled(enableProfiling) { "Checking for command map commands took ${System.nanoTime() - start}ns for ${author.idLong}" }

			start = System.nanoTime()
			if (loritta.legacyCommandManager.matches(lorittaMessageEvent, rawArguments, serverConfig, locale, lorittaUser))
				return true
			logIfEnabled(enableProfiling) { "Checking for legacy command manager commands took ${System.nanoTime() - start}ns for ${author.idLong}" }
		}

		return false
	}

	/**
	 * Checks if the message contains only a mention for me
	 *
	 * @param contentRaw the raw content of the message
	 * @returns if the message is mentioning only me
	 */
	fun isMentioningOnlyMe(contentRaw: String): Boolean = contentRaw.replace("!", "").trim() == "<@${loritta.discordConfig.discord.clientId}>"

	fun logIfEnabled(doLog: Boolean, msg: () -> Any?) {
		if (doLog)
			logger.info(msg)
	}

	fun <T> Deferred<T>.logOnCompletion(doLog: Boolean, msg: () -> Any?): Deferred<T> {
		val start = System.nanoTime()

		if (doLog)
			this.invokeOnCompletion {
				logger.info(msg.invoke().toString().replace("{time}", (System.nanoTime() - start).toString()))
			}

		return this
	}
}
