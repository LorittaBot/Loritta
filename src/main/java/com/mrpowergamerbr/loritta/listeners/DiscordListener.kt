package com.mrpowergamerbr.loritta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import com.github.kevinsawicki.http.HttpRequest
import com.github.salomonbrys.kotson.get
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.toJsonArray
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.commands.vanilla.administration.MuteCommand
import com.mrpowergamerbr.loritta.dao.Mute
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.modules.AutoroleModule
import com.mrpowergamerbr.loritta.modules.ReactionModule
import com.mrpowergamerbr.loritta.modules.StarboardModule
import com.mrpowergamerbr.loritta.modules.WelcomeModule
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.GitHubIssues
import com.mrpowergamerbr.loritta.tables.GuildProfiles
import com.mrpowergamerbr.loritta.tables.Mutes
import com.mrpowergamerbr.loritta.userdata.MemberCounterConfig
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.config.EnvironmentType
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.ChannelType
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.TextChannel
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
import net.perfectdreams.loritta.tables.Giveaways
import net.perfectdreams.loritta.tables.ReactionOptions
import net.perfectdreams.loritta.utils.giveaway.GiveawayManager
import okio.Buffer
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.kotlin.utils.getOrPutNullable
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.TimeUnit

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

		val issueMutex = Mutex()

		private val logger = KotlinLogging.logger {}
		private val requestLogger = LoggerFactory.getLogger("requests")

		suspend fun isSuggestionValid(message: Message, requiredCount: Int = 5): Boolean {
			// Pegar o número de likes - dislikes
			val reactionCount = (message.reactions.firstOrNull { it.reactionEmote.isEmote("\uD83D\uDC4D") }?.retrieveUsers()?.await()?.filter { !it.isBot }?.size ?: 0) - (message.reactions.firstOrNull { it.reactionEmote.isEmote("\uD83D\uDC4E") }?.retrieveUsers()?.await()?.filter { !it.isBot }?.size ?: 0)
			return reactionCount >= requiredCount
		}

		fun sendSuggestionToGitHub(message: Message) {
			var issueTitle = message.contentStripped

			while (issueTitle.length > 50) {
				if (issueTitle.contains(":")) {
					issueTitle = issueTitle.split(":").first()
					continue
				}
				if (issueTitle.contains("\n")) {
					issueTitle = issueTitle.split("\n").first()
					continue
				}

				issueTitle = issueTitle.substringIfNeeded(0 until 77)
				break
			}

			val labels = mutableListOf<String>()

			if (message.contentRaw.contains("bug", true) || message.contentRaw.contains("problema", true)) {
				labels.add("\uD83D\uDC1E bug")
			}

			if (message.contentRaw.contains("adicionar", true) || message.contentRaw.contains("colocar", true) || message.contentRaw.contains("fazer", true)) {
				labels.add("✨ enhancement")
			}

			var suggestionBody = message.contentRaw

			message.emotes.forEach {
				suggestionBody = suggestionBody.replace(it.asMention, "<img src=\"${it.imageUrl}\" width=\"16\">")
			}
			message.mentionedUsers.forEach {
				suggestionBody = suggestionBody.replace(it.asMention, "`@${it.name}#${it.discriminator}` (`${it.id}`)")
			}
			message.mentionedChannels.forEach {
				suggestionBody = suggestionBody.replace(it.asMention, "`#${it.name}` (`${it.id}`)")
			}
			message.mentionedRoles.forEach {
				suggestionBody = suggestionBody.replace(it.asMention, "`@${it.name}` (`${it.id}`)")
			}
			// Encontrar links na sugestão
			val regex = Constants.HTTP_URL_PATTERN.toRegex()
			val regexMatch = regex.findAll(suggestionBody)

			// Agora vamos fazer com que imagens do imgur (e imagens do discord em forma de link) funcionem!
			// Se não for um link do imgur e imagem do discord em forma de link, então o link vai ficar em markdown
			regexMatch.forEach {
				if (it.value.contains("i.imgur")) {
					// Precisamos substituir o sufixo do link, caso seja um gif, para a imagem seja valída
					if (!it.value.endsWith(".gifv"))
						suggestionBody = suggestionBody.replace(it.value, "![${it.value}](${it.value})")
					else {
						val link = it.value.replace(".gifv", ".gif")
						suggestionBody = suggestionBody.replace(link, "![$link]($link)")
					}
				}
				else if (it.value.contains("cdn.discordapp.com") && (!it.value.contains("/emojis/")))
					suggestionBody = suggestionBody.replace(it.value, "![${it.value}](${it.value})")
				else
					if (!it.value.contains("cdn.discordapp.com/emojis/"))
						suggestionBody = suggestionBody.replace(it.value, "`${it.value}`")
			}

			val body = """<img width="64" align="left" src="${message.author.effectiveAvatarUrl}">
    |
    |**Sugestão de `${message.author.name}#${message.author.discriminator}` (`${message.author.id}`)**
    |**ID da Mensagem: `${message.channel.id}-${message.idLong}`**
    |
    |<hr>
    |
    |$suggestionBody
    |
    |${message.attachments.filter { !it.isImage }.joinToString("\n", transform = { it.url })}
    |${message.attachments.filter { it.isImage }.joinToString("\n", transform = { "![${it.url}](${it.url})" })}
""".trimMargin()

			val request = HttpRequest.post("${Loritta.config.github.repositoryUrl}/issues")
					.header("Authorization", "token ${Loritta.config.github.apiKey}")
					.accept("application/vnd.github.symmetra-preview+json")
					.send(
							gson.toJson(
									jsonObject(
											"title" to issueTitle,
											"body" to body,
											"labels" to labels.toJsonArray()
									)
							)
					)

			val json = jsonParser.parse(request.body())

			val issueId = json["number"].long
			transaction(Databases.loritta) {
				GitHubIssues.insert {
					it[messageId] = message.idLong
					it[githubIssueId] = issueId
				}
			}
		}
	}

	override fun onHttpRequest(event: HttpRequestEvent) {
		val copy = event.requestRaw?.newBuilder()?.build()
		val buffer = Buffer()
		copy?.body()?.writeTo(buffer)
		requestLogger.info("${event.route.method.name} ${event.route.compiledRoute}\nGlobally? ${event.responseHeaders?.get("X-RateLimit-Global") ?: "false"} - RateLimit-Limit: ${event.responseHeaders?.get("X-RateLimit-Limit")} - RateLimit-Remaining: ${event.responseHeaders?.get("X-RateLimit-Remaining")} - Retry-After: ${event.responseHeaders?.get("Retry-After")}\n${buffer.readUtf8()}")
	}

	override fun onGuildMessageReactionAdd(event: GuildMessageReactionAddEvent) {
		if (event.user.isBot)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			if (Loritta.config.loritta.environment == EnvironmentType.CANARY) {
				if (event.channel.id == "359139508681310212" && (event.reactionEmote.isEmote("\uD83D\uDC4D") || event.reactionEmote.isEmote("\uD83D\uDC4E"))) { // Canal de sugestões
					issueMutex.withLock {
						val alreadySent = transaction(Databases.loritta) {
							GitHubIssues.select { GitHubIssues.messageId eq event.messageIdLong }.count() != 0
						}

						if (alreadySent)
							return@withLock

						val message = event.channel.retrieveMessageById(event.messageId).await()

						if (isSuggestionValid(message)) {
							sendSuggestionToGitHub(message)
						}
					}
				}
			}

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
					LorittaUtilsKotlin.sendStackTrace("[`${e.guild.name}`] **Starboard ${e.member?.user?.name}**", exception)
				}
			}
		}
	}

	override fun onGuildLeave(e: GuildLeaveEvent) {
		// Remover threads de role removal caso a Loritta tenha saido do servidor
		val toRemove = mutableListOf<String>()
		MuteCommand.roleRemovalJobs.forEach { key, value ->
			if (key.startsWith(e.guild.id)) {
				value.cancel()
				toRemove.add(key)
			}
		}
		toRemove.forEach { MuteCommand.roleRemovalJobs.remove(it) }

		loritta.executor.execute {
			// Quando a Loritta sair de uma guild, automaticamente remova o ServerConfig daquele servidor
			loritta.serversColl.deleteOne(Filters.eq("_id", e.guild.id))

			transaction(Databases.loritta) {
				// Deletar todos os perfis do servidor
				GuildProfiles.deleteWhere {
					GuildProfiles.guildId eq e.guild.idLong
				}

				// Deletar configurações
				val serverConfig = ServerConfig.findById(e.guild.idLong)
				val donationConfig = serverConfig?.donationConfig

				serverConfig?.delete()
				donationConfig?.delete()
			}
		}
	}

	override fun onGuildJoin(event: GuildJoinEvent) {
		// Vamos alterar a minha linguagem quando eu entrar em um servidor, baseando na localização dele
		val region = event.guild.region
		val regionName = region.getName()
		val serverConfig = loritta.getServerConfigForGuild(event.guild.id)

		event.guild.region
		// Portuguese
		if (regionName.startsWith("Brazil")) {
			serverConfig.localeId = "default"
		} else {
			serverConfig.localeId = "en-us"
		}

		// Adicionar a permissão de DJ para alguns cargos
		event.guild.roles.forEach {
			if (it.hasPermission(Permission.ADMINISTRATOR) || it.hasPermission(Permission.MANAGE_SERVER)) {
				serverConfig.permissionsConfig.roles[it.id] = PermissionsConfig.PermissionRole().apply {
					this.permissions.add(LorittaPermission.DJ)
				}
			}
		}

		// E depois iremos salvar a configuração do servidor
		loritta.executor.execute {
			loritta save serverConfig
		}
	}

	override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val conf = loritta.getServerConfigForGuild(event.guild.id)

				if (conf.moderationConfig.useLorittaBansNetwork && loritta.networkBanManager.getNetworkBanEntry(event.user.id) != null) {
					val entry = loritta.networkBanManager.getNetworkBanEntry(event.user.id)!! // oof
					BanCommand.ban(
							conf,
							event.guild,
							event.guild.selfMember.user,
							com.mrpowergamerbr.loritta.utils.loritta.getLegacyLocaleById(conf.localeId),
							event.user,
							entry.reason,
							false,
							7
					)
					return@launch
				}

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

				for (eventHandler in conf.nashornEventHandlers) {
					eventHandler.handleMemberJoin(event)
				}

				if (conf.autoroleConfig.isEnabled && event.guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) { // Está ativado?
					AutoroleModule.giveRoles(event, conf.autoroleConfig)
				}

				if (conf.joinLeaveConfig.isEnabled) { // Está ativado?
					WelcomeModule.handleJoin(event, conf)
				}

				val mute = transaction(Databases.loritta) {
					Mute.find { (Mutes.guildId eq event.guild.idLong) and (Mutes.userId eq event.member.user.idLong) }.firstOrNull()
				}

				if (mute != null) {
					val locale = loritta.getLegacyLocaleById(conf.localeId)
					val muteRole = MuteCommand.getMutedRole(event.guild, loritta.getLegacyLocaleById(conf.localeId)) ?: return@launch

					event.guild.controller.addSingleRoleToMember(event.member, muteRole).queue()

					if (mute.isTemporary)
						MuteCommand.spawnRoleRemovalThread(event.guild, locale, event.user, mute.expiresAt!!)
				}
			} catch (e: Exception) {
				logger.error("[${event.guild.name}] Ao entrar no servidor ${event.user.name}", e)
				LorittaUtilsKotlin.sendStackTrace("[`${event.guild.name}`] **Ao entrar no servidor ${event.user.name}**", e)
			}
		}
	}

	override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
		if (DebugLog.cancelAllEvents)
			return

		// Remover thread de role removal caso o usuário tenha saido do servidor
		val job = MuteCommand.roleRemovalJobs[event.guild.id + "#" + event.member.user.id]
		job?.cancel()
		MuteCommand.roleRemovalJobs.remove(event.guild.id + "#" + event.member.user.id)

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				if (event.user.id == Loritta.config.discord.clientId) {
					return@launch
				}

				val conf = loritta.getServerConfigForGuild(event.guild.id)

				queueTextChannelTopicUpdates(event.guild, conf, true)

				for (eventHandler in conf.nashornEventHandlers) {
					eventHandler.handleMemberLeave(event)
				}

				if (conf.joinLeaveConfig.isEnabled) {
					WelcomeModule.handleLeave(event, conf)
				}
			} catch (e: Exception) {
				logger.error("[${event.guild.name}] Ao sair do servidor ${event.user.name}", e)
				LorittaUtilsKotlin.sendStackTrace("[`${event.guild.name}`] **Ao sair do servidor ${event.user.name}**", e)
			}
		}
	}

	fun queueTextChannelTopicUpdates(guild: Guild, serverConfig: MongoServerConfig, hideInEventLog: Boolean = false) {
		for (textChannel in guild.textChannels) {
			queueTextChannelTopicUpdate(guild, serverConfig, textChannel, hideInEventLog)
		}
	}

	fun queueTextChannelTopicUpdate(guild: Guild, serverConfig: MongoServerConfig, textChannel: TextChannel, hideInEventLog: Boolean = false) {
		if (!guild.selfMember.hasPermission(textChannel, Permission.MANAGE_CHANNEL))
			return

		val memberCountConfig = serverConfig.getTextChannelConfig(textChannel).memberCounterConfig ?: return

		val lastUpdate = memberCounterLastUpdate[textChannel.idLong] ?: 0L
		val diff = System.currentTimeMillis() - lastUpdate

		if (MEMBER_COUNTER_COOLDOWN > diff) { // Para evitar rate limits ao ter muitas entradas/saídas ao mesmo tempo, vamos esperar 60s entre cada update
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

	fun updateTextChannelTopic(guild: Guild, serverConfig: MongoServerConfig, textChannel: TextChannel, memberCounterConfig: MemberCounterConfig, hideInEventLog: Boolean = false) {
		val formattedTopic = memberCounterConfig.getFormattedTopic(guild)
		if (hideInEventLog)
			memberCounterJoinLeftCache.add(textChannel.idLong)
		memberCounterLastUpdate[textChannel.idLong] = System.currentTimeMillis()

		val locale = loritta.getLocaleById(serverConfig.localeId)
		textChannel.manager.setTopic(formattedTopic).reason(locale["loritta.modules.counter.auditLogReason"]).queue()
	}

	override fun onGuildReady(event: GuildReadyEvent) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
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
				MuteCommand.spawnRoleRemovalThread(guild, com.mrpowergamerbr.loritta.utils.loritta.getLegacyLocaleById("default"), member.user, mute.expiresAt!!)
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

				val allActiveGiveaways = transaction(Databases.loritta) {
					Giveaway.find { Giveaways.guildId eq event.guild.idLong }.toMutableList()
				}

				allActiveGiveaways.forEach {
					try {
						if (GiveawayManager.giveawayTasks[it.id.value] == null)
							GiveawayManager.createGiveawayJob(it)
					} catch (e: Exception) {
						logger.error(e) { "Error while creating giveaway ${it.id.value} job on guild ready ${event.guild.idLong}" }
					}
				}
			}
		}
	}
}