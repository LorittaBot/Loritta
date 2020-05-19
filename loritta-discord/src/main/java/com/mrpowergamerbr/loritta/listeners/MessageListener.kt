package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.AutoroleModule
import com.mrpowergamerbr.loritta.modules.Modules
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.eventlog.EventLog
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.events.message.MessageDeleteEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.api.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.perfectdreams.loritta.dao.servers.moduleconfigs.AutoroleConfig
import net.perfectdreams.loritta.platform.discord.plugin.DiscordPlugin
import net.perfectdreams.loritta.platform.discord.plugin.LorittaDiscordPlugin
import net.perfectdreams.loritta.tables.BlacklistedGuilds
import net.perfectdreams.loritta.utils.Emotes
import org.apache.commons.text.similarity.LevenshteinDistance
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class MessageListener(val loritta: Loritta) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
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
		if (loritta.isMainAccountOnlineAndWeAreNotTheMainAccount(event.guild))
			return

		if (loritta.discordConfig.discord.disallowBots && !loritta.discordConfig.discord.botWhitelist.contains(event.author.idLong) && event.author.isBot) // Se uma mensagem de um bot, ignore a mensagem!
			return

		if (DebugLog.cancelAllEvents)
			return

		if (event.message.type != MessageType.DEFAULT) // Existem vários tipos de mensagens no Discord, mas apenas estamos interessados nas mensagens padrões de texto
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val member = event.member
				if (member == null) { // This may seem dumb, but it works!
					logger.warn { "${event.author} saiu do servidor ${event.guild.id} antes de eu poder processar a mensagem"}
					return@launch
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
				// Se o dono do servidor for o usuário que está executando o comando, não é necessário pegar o perfil novamente
				val ownerProfile = if (event.guild.ownerIdLong == member.idLong) lorittaProfile else loritta.getLorittaProfile(event.guild.ownerIdLong)
				logIfEnabled(enableProfiling) { "Loading owner's profile took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				val locale = loritta.getLocaleById(serverConfig.localeId)
				logIfEnabled(enableProfiling) { "Loading ${serverConfig.localeId} locale took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				val legacyLocale = loritta.getLegacyLocaleById(serverConfig.localeId)
				logIfEnabled(enableProfiling) { "Loading ${serverConfig.localeId} legacy locale took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				// We use "loadMemberRolesLorittaPermissions(...)" to avoid unnecessary retrievals later on, because we recheck the role permission later
				val rolesLorittaPermissions = LorittaUser.loadMemberRolesLorittaPermissions(serverConfig, member)
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
				if (ownerProfile != null && isOwnerBanned(ownerProfile, event.guild))
					return@launch
				logIfEnabled(enableProfiling) { "Checking for owner profile ban took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				if (isGuildBanned(event.guild))
					return@launch
				logIfEnabled(enableProfiling) { "Checking for guild ban took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				EventLog.onMessageReceived(serverConfig, event.message)
				logIfEnabled(enableProfiling) { "Logging to EventLog took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()

				if (chance(25.0) && event.guild.selfMember.hasPermission(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI) && isMentioningMe(event.message))
					event.message.addReaction("smol_lori_putassa_ping:397748526362132483").queue()

				logIfEnabled(enableProfiling) { "Checking user mention took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				if (isMentioningOnlyMe(event.message.contentRaw)) {
					var response = legacyLocale["MENTION_RESPONSE", member.asMention, serverConfig.commandPrefix]

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
							response = legacyLocale["MENTION_ResponseEveryoneBlocked", event.message.author.asMention, serverConfig.commandPrefix]
						else
							response = legacyLocale["MENTION_ResponseRoleBlocked", event.message.author.asMention, serverConfig.commandPrefix, ignoringCommandsRole?.asMention]
					} else {
						if (serverConfig.blacklistedChannels.contains(event.channel.idLong) && !lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
							// Vamos pegar um canal que seja possível usar comandos
							val useCommandsIn = event.guild.textChannels.firstOrNull { !serverConfig.blacklistedChannels.contains(it.idLong) && it.canTalk(member) }

							response = if (useCommandsIn != null) {
								// Canal não bloqueado!
								legacyLocale["MENTION_ResponseBlocked", event.message.author.asMention, serverConfig.commandPrefix, useCommandsIn.asMention]
							} else {
								// Nenhum canal disponível...
								legacyLocale["MENTION_ResponseBlockedNoChannels", event.message.author.asMention, serverConfig.commandPrefix]
							}
						}
					}
					if (event.channel.canTalk()) {
						event.channel.sendMessage("<:loritta:331179879582269451> **|** $response").queue()
					} else {
						event.author.openPrivateChannel().queue {
							it.sendMessage("<:loritta:331179879582269451> **|** $response").queue()
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
						legacyLocale,
						lorittaUser
				)
				logIfEnabled(enableProfiling) { "Creating a LorittaMessageEvent took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()

				if (autoroleConfig != null && autoroleConfig.enabled && autoroleConfig.giveOnlyAfterMessageWasSent && event.guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) // Está ativado?
					AutoroleModule.giveRoles(member, autoroleConfig)

				logIfEnabled(enableProfiling) { "Giving auto role on message took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				for (module in (MESSAGE_RECEIVED_MODULES + loritta.pluginManager.plugins.filterIsInstance<DiscordPlugin>().flatMap { it.messageReceivedModules } + loritta.pluginManager.plugins.filterIsInstance<LorittaDiscordPlugin>().flatMap { it.messageReceivedModules })) {
					start = System.nanoTime()
					if (module.matches(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, legacyLocale) && module.handle(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, legacyLocale))
						return@launch
					logIfEnabled(enableProfiling) { "Executing ${module::class.simpleName} took ${System.nanoTime() - start}ns for ${event.author.idLong}" }
				}

				start = System.nanoTime()
				if (lorittaUser.hasPermission(LorittaPermission.IGNORE_COMMANDS))
					return@launch
				logIfEnabled(enableProfiling) { "Checking for ignore permission took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				if (lorittaProfile != null && isUserStillBanned(lorittaProfile))
					return@launch
				logIfEnabled(enableProfiling) { "Checking for user ban took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				// Executar comandos
				start = System.nanoTime()
				if (checkCommandsAndDispatch(lorittaMessageEvent, serverConfig, locale, legacyLocale, lorittaUser))
					return@launch
				logIfEnabled(enableProfiling) { "All commands check took ${System.nanoTime() - start}ns for ${event.author.idLong}" }

				start = System.nanoTime()
				loritta.messageInteractionCache.values.toMutableList().forEach {
					if (it.onMessageReceived != null)
						it.onMessageReceived!!.invoke(lorittaMessageEvent)

					if (it.guildId == event.guild.idLong && it.channelId == event.channel.idLong) {
						if (it.onResponse != null)
							it.onResponse!!.invoke(lorittaMessageEvent)

						if (it.onResponseByAuthor != null) {
							if (it.originalAuthor == event.author.id)
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
								LoriReply(
										"${legacyLocale["LORITTA_UnknownCommand", command, "${serverConfig.commandPrefix}${legacyLocale["AJUDA_CommandName"]}"]} ${Emotes.LORI_OWO}",
										"\uD83E\uDD37"
								)
						)

						val allCommandLabels = mutableListOf<String>()

						loritta.commandMap.commands.forEach {
							if (!it.onlyOwner && !serverConfig.disabledCommands.contains(it.javaClass.simpleName))
								allCommandLabels.addAll(it.labels)
						}

						loritta.commandManager.commands.forEach {
							if (!it.onlyOwner && !serverConfig.disabledCommands.contains(it.javaClass.simpleName))
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
									LoriReply(
											prefix = Emotes.LORI_HM,
											message = locale["commands.didYouMeanCommand", serverConfig.commandPrefix + nearestCommand],
											mentionUser = false
									)
							)
						}

						event.channel.sendMessage(list.joinToString("\n") { it.build(event.author) }).queue {
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

		if (event.message.type != MessageType.DEFAULT) // Existem vários tipos de mensagens no Discord, mas apenas estamos interessados nas mensagens padrões de texto
			return

		if (loritta.rateLimitChecker.checkIfRequestShouldBeIgnored())
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			val serverConfig = loritta.getOrCreateServerConfig(-1, true)
			val profile = loritta.getOrCreateLorittaProfile(event.author.idLong) // Carregar perfil do usuário
			val lorittaUser = LorittaUser(event.author, EnumSet.noneOf(LorittaPermission::class.java), profile)
			// TODO: Usuários deverão poder escolher a linguagem que eles preferem via mensagem direta
			val locale = loritta.getLocaleById(serverConfig.localeId)
			val legacyLocale = loritta.getLegacyLocaleById("default")

			if (isUserStillBanned(profile))
				return@launch

			if (isMentioningOnlyMe(event.message.contentRaw)) {
				event.channel.sendMessage(legacyLocale["LORITTA_CommandsInDirectMessage", event.message.author.asMention, legacyLocale["AJUDA_CommandName"]]).queue()
				return@launch
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
					legacyLocale,
					lorittaUser
			)

			// Executar comandos
			if (checkCommandsAndDispatch(lorittaMessageEvent, serverConfig, locale, legacyLocale, lorittaUser))
				return@launch
		}
	}

	override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
		if (loritta.isMainAccountOnlineAndWeAreNotTheMainAccount(event.guild))
			return

		if (event.author.isBot)
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
				val legacyLocale = loritta.getLegacyLocaleById(serverConfig.localeId)
				val locale = loritta.getLocaleById(serverConfig.localeId)
				val permissions = LorittaUser.loadMemberLorittaPermissions(serverConfig, member)
				val lorittaUser = GuildLorittaUser(member, permissions, lorittaProfile)

				EventLog.onMessageUpdate(serverConfig, legacyLocale, event.message)

				val lorittaMessageEvent = LorittaMessageEvent(
						event.author,
						event.member,
						event.message,
						event.messageId,
						event.guild,
						event.channel,
						event.channel,
						serverConfig,
						legacyLocale,
						lorittaUser
				)

				for (module in (MESSAGE_EDITED_MODULES + loritta.pluginManager.plugins.filterIsInstance<DiscordPlugin>().flatMap { it.messageEditedModules } + loritta.pluginManager.plugins.filterIsInstance<LorittaDiscordPlugin>().flatMap { it.messageEditedModules })) {
					if (module.matches(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, legacyLocale) && module.handle(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, legacyLocale))
						return@launch
				}

				if (checkCommandsAndDispatch(lorittaMessageEvent, serverConfig, locale, legacyLocale, lorittaUser))
					return@launch
			}
		}
	}

	override fun onMessageDelete(event: MessageDeleteEvent) {
		loritta.messageInteractionCache.remove(event.messageIdLong)
	}

	/**
	 * Checks and dispatches commands
	 *
	 * @return if a command was dispatched
	 */
	suspend fun checkCommandsAndDispatch(lorittaMessageEvent: LorittaMessageEvent, serverConfig: ServerConfig, locale: BaseLocale, legacyLocale: LegacyBaseLocale, lorittaUser: LorittaUser): Boolean {
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
			if (loritta.commandMap.dispatch(lorittaMessageEvent, rawArguments, serverConfig, locale, legacyLocale, lorittaUser))
				return true
			logIfEnabled(enableProfiling) { "Checking for command map commands took ${System.nanoTime() - start}ns for ${author.idLong}" }

			start = System.nanoTime()
			if (loritta.commandManager.dispatch(lorittaMessageEvent, rawArguments, serverConfig, locale, legacyLocale, lorittaUser))
				return true
			logIfEnabled(enableProfiling) { "Checking for command manager commands took ${System.nanoTime() - start}ns for ${author.idLong}" }

			start = System.nanoTime()
			if (loritta.legacyCommandManager.matches(lorittaMessageEvent, rawArguments, serverConfig, locale, legacyLocale, lorittaUser))
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

	/**
	 * Checks if the message mentions me
	 *
	 * @param contentRaw the message
	 * @returns if the message is mentioning me
	 */
	fun isMentioningMe(message: Message): Boolean = message.isMentioned(message.guild.selfMember)

	/**
	 * Checks if the owner of the guild is banned and, if true, makes me quit the server
	 *
	 * @param ownerProfile the profile of the guild's owner
	 * @param guild        the guild
	 * @return if the owner of the guild is banned
	 */
	fun isOwnerBanned(ownerProfile: Profile, guild: Guild): Boolean {
		if (ownerProfile.isBanned) { // Se o dono está banido...
			if (!loritta.config.isOwner(ownerProfile.userId)) { // E ele não é o dono do bot!
				logger.info("Eu estou saindo do servidor ${guild.name} (${guild.id}) já que o dono ${ownerProfile.userId} está banido de me usar! ᕙ(⇀‸↼‶)ᕗ")
				guild.leave().queue() // Então eu irei sair daqui, me recuso a ficar em um servidor que o dono está banido! ᕙ(⇀‸↼‶)ᕗ
				return true
			}
		}
		return false
	}

	/**
	 * Checks if the guild is blacklisted and, if yes, makes me quit the server
	 *
	 * @param guild        the guild
	 * @return if the owner of the guild is banned
	 */
	fun isGuildBanned(guild: Guild): Boolean {
		val blacklisted = transaction(Databases.loritta) {
			BlacklistedGuilds.select {
				BlacklistedGuilds.id eq guild.idLong
			}.firstOrNull()
		}

		if (blacklisted != null) { // Se o servidor está banido...
			if (!loritta.config.isOwner(guild.owner!!.user.id)) { // E ele não é o dono do bot!
				logger.info("Eu estou saindo do servidor ${guild.name} (${guild.id}) já que o servidor está banido de me usar! ᕙ(⇀‸↼‶)ᕗ *${blacklisted[BlacklistedGuilds.reason]}")
				guild.leave().queue() // Então eu irei sair daqui, me recuso a ficar em um servidor que o dono está banido! ᕙ(⇀‸↼‶)ᕗ
				return true
			}
		}
		return false
	}

	/**
	 * Checks if the user is still banned, if not, remove it from the ignore list
	 *
	 * @param profile the profile of the user
	 * @return if the user is still banned
	 */
	fun isUserStillBanned(profile: Profile): Boolean {
		if (loritta.ignoreIds.contains(profile.userId)) { // Se o usuário está sendo ignorado...
			if (profile.isBanned) { // E ele ainda está banido...
				logger.info { "${profile.id} tried to use me, but they are banned! >:)" }
				return true // Então flw galerinha
			} else {
				// Se não, vamos remover ele da lista do ignoreIds
				loritta.ignoreIds.remove(profile.userId)
				return false
			}
		}
		return false
	}

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