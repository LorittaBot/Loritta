package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.*
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import mu.KotlinLogging
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.events.message.MessageDeleteEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.util.regex.Pattern

class MessageListener(val loritta: Loritta) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
		if (event.author.isBot) // Se uma mensagem de um bot, ignore a mensagem!
			return

		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			try {
				val member = event.member
				if (member == null) {
					logger.warn { "${event.author} saiu do servidor ${event.guild.id} antes de eu poder processar a mensagem"}
					return@execute
				}

				val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
				val lorittaProfile = loritta.getLorittaProfileForUser(event.author.id)
				val ownerProfile = loritta.getLorittaProfileForUser(event.guild.owner.user.id)
				val locale = loritta.getLocaleById(serverConfig.localeId)
				val lorittaUser = GuildLorittaUser(member, serverConfig, lorittaProfile)

				lorittaProfile.isAfk = false
				lorittaProfile.afkReason = null

				if (isOwnerBanned(ownerProfile, event.guild))
					return@execute

				if (isMentioningOnlyMe(event.message.contentRaw)) {
					var response = locale["MENTION_RESPONSE", member.asMention, serverConfig.commandPrefix]

					if (lorittaUser.hasPermission(LorittaPermission.IGNORE_COMMANDS)) {
						// Usuário não pode usar comandos

						// Qual é o cargo que não permite utilizar os meus comandos?
						val roles = member.roles.toMutableList()

						val everyone = member.guild.publicRole
						if (everyone != null) {
							roles.add(everyone)
						}

						roles.sortedByDescending { it.position }

						var ignoringCommandsRole: Role? = null
						for (role in roles) {
							val permissionRole = serverConfig.permissionsConfig.roles.getOrDefault(role.id, PermissionsConfig.PermissionRole())
							if (permissionRole.permissions.contains(LorittaPermission.IGNORE_COMMANDS)) {
								ignoringCommandsRole = role
								break
							}
						}

						if (ignoringCommandsRole == event.guild.publicRole)
							response = locale["MENTION_ResponseEveryoneBlocked", event.message.author.asMention, serverConfig.commandPrefix]
						else
							response = locale["MENTION_ResponseRoleBlocked", event.message.author.asMention, serverConfig.commandPrefix, ignoringCommandsRole?.asMention]
					} else {
						if (serverConfig.blacklistedChannels.contains(event.channel.id) && !lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
							// Vamos pegar um canal que seja possível usar comandos
							val useCommandsIn = event.guild.textChannels.firstOrNull { !serverConfig.blacklistedChannels.contains(it.id) && it.canTalk(member) }

							response = if (useCommandsIn != null) {
								// Canal não bloqueado!
								locale["MENTION_ResponseBlocked", event.message.author.asMention, serverConfig.commandPrefix, useCommandsIn.asMention]
							} else {
								// Nenhum canal disponível...
								locale["MENTION_ResponseBlockedNoChannels", event.message.author.asMention, serverConfig.commandPrefix]
							}
						}
					}

					event.channel.sendMessage("<:loritta:331179879582269451> **|** $response").complete()
				}

				val modules = listOf(
						SlowModeModule(),
						InviteLinkModule(),
						ServerSupportModule(),
						AutomodModule(),
						ExperienceModule(),
						AminoConverterModule(),
						AFKModule(),
						BomDiaECiaModule()
				)

				val lorittaMessageEvent = LorittaMessageEvent(
						event.author,
						member,
						event.message,
						event.messageId,
						event.guild,
						event.channel,
						event.channel
				)

				for (module in modules) {
					if (module.matches(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, locale) && module.handle(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, locale))
						return@execute
				}

				for (eventHandler in serverConfig.nashornEventHandlers)
					eventHandler.handleMessageReceived(event, serverConfig)

				// emotes favoritos
				event.message.emotes.forEach {
					lorittaProfile.usedEmotes.put(it.id, lorittaProfile.usedEmotes.getOrDefault(it.id, 0) + 1)
				}

				loritta save lorittaProfile

				if (lorittaUser.hasPermission(LorittaPermission.IGNORE_COMMANDS))
					return@execute

				if (isUserStillBanned(lorittaProfile))
					return@execute

				// Primeiro os comandos vanilla da Loritta(tm)
				loritta.commandManager.commandMap.filter { !serverConfig.disabledCommands.contains(it.javaClass.simpleName) }.forEach { cmd ->
					if (cmd.matches(lorittaMessageEvent, serverConfig, locale, lorittaUser)) {
						return@execute
					}
				}

				// E depois os comandos usando JavaScript (Nashorn)
				serverConfig.nashornCommands.forEach { cmd ->
					if (cmd.matches(lorittaMessageEvent, serverConfig, locale, lorittaUser)) {
						return@execute
					}
				}

				loritta.messageInteractionCache.values.forEach {
					if (it.onMessageReceived != null)
						it.onMessageReceived!!.invoke(lorittaMessageEvent)

					if (it.guild == event.guild.id) {
						if (it.onResponse != null)
							it.onResponse!!.invoke(lorittaMessageEvent)

						if (it.onResponseByAuthor != null) {
							if (it.originalAuthor == event.author.id)
								it.onResponseByAuthor!!.invoke(lorittaMessageEvent)
						}
					}
				}

				if (event.channel.canTalk() && serverConfig.warnOnUnknownCommand) {
					val startsWithCommandPattern = Regex("^" + Pattern.quote(serverConfig.commandPrefix) + "[A-z0-9]+.*")

					if (event.message.contentRaw.matches(startsWithCommandPattern)) {
						val command = event.message.contentDisplay.split(" ")[0].stripCodeMarks()
						val message = event.channel.sendMessage("\uD83E\uDD37 **|** ${event.author.asMention} ${locale["LORITTA_UnknownCommand", command, "${serverConfig.commandPrefix}${locale["AJUDA_CommandName"]}"]} <:blobBlush:357977010771066890>").complete()
						Thread.sleep(5000)
						message.delete().queue()
					}
				}
			} catch (e: Exception) {
				logger.error("[${event.guild.name}] Erro ao processar mensagem de ${event.author.name} (${event.author.id} - ${event.message.contentRaw}", e)
				LorittaUtilsKotlin.sendStackTrace(event.message, e)
			}
		}
	}

	override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
		loritta.executor.execute {
			val serverConfig = LorittaLauncher.loritta.dummyServerConfig
			val profile = loritta.getLorittaProfileForUser(event.author.id) // Carregar perfil do usuário
			val lorittaUser = LorittaUser(event.author, serverConfig, profile)
			// TODO: Usuários deverão poder escolher a linguagem que eles preferem via mensagem direta
			val locale = loritta.getLocaleById("default")

			if (isUserStillBanned(profile))
				return@execute

			if (isMentioningOnlyMe(event.message.contentRaw)) {
				event.channel.sendMessage(locale["LORITTA_CommandsInDirectMessage", event.message.author.asMention, locale["AJUDA_CommandName"]]).complete()
				return@execute
			}

			val lorittaMessageEvent = LorittaMessageEvent(
					event.author,
					null,
					event.message,
					event.messageId,
					null,
					event.channel,
					null
			)

			// Comandos vanilla da Loritta
			loritta.commandManager.commandMap.forEach { cmd ->
				if (cmd.matches(lorittaMessageEvent, serverConfig, locale, lorittaUser)) {
					return@execute
				}
			}
		}
	}

	override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
		if (event.author.isBot)
			return

		if (DebugLog.cancelAllEvents)
			return

		if (event.channel.type == ChannelType.TEXT) { // Mensagens em canais de texto
			loritta.executor.execute {
				val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
				val lorittaProfile = loritta.getLorittaProfileForUser(event.author.id)
				val locale = loritta.getLocaleById(serverConfig.localeId)
				val lorittaUser = GuildLorittaUser(event.member, serverConfig, lorittaProfile)

				val modules = listOf(
						ServerSupportModule(),
						AutomodModule(),
						InviteLinkModule()
				)

				val lorittaMessageEvent = LorittaMessageEvent(
						event.author,
						event.member,
						event.message,
						event.messageId,
						event.guild,
						event.channel,
						event.channel
				)

				for (module in modules) {
					if (module.matches(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, locale) && module.handle(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, locale))
						return@execute
				}

				// Primeiro os comandos vanilla da Loritta(tm)
				loritta.commandManager.commandMap.filter{ !serverConfig.disabledCommands.contains(it.javaClass.simpleName) }.forEach { cmd ->
					if (cmd.matches(lorittaMessageEvent, serverConfig, locale, lorittaUser)) {
						return@execute
					}
				}

				// E depois os comandos usando JavaScript (Nashorn)
				serverConfig.nashornCommands.forEach { cmd ->
					if (cmd.matches(lorittaMessageEvent, serverConfig, locale, lorittaUser)) {
						return@execute
					}
				}
			}
		}
	}

	override fun onMessageDelete(event: MessageDeleteEvent) {
		loritta.messageInteractionCache.remove(event.messageId)
	}

	/**
	 * Checks if the message contains only a mention for me
	 *
	 * @param contentRaw the raw content of the message
	 * @returns if the message is mentioning only me
	 */
	fun isMentioningOnlyMe(contentRaw: String): Boolean = contentRaw.replace("!", "").trim() == "<@${Loritta.config.clientId}>"

	/**
	 * Checks if the owner of the guild is banned and, if true, makes me quit the server
	 *
	 * @param ownerProfile the profile of the guild's owner
	 * @param guild        the guild
	 * @return if the owner of the guild is banned
	 */
	fun isOwnerBanned(ownerProfile: LorittaProfile, guild: Guild): Boolean {
		if (ownerProfile.isBanned) { // Se o dono está banido...
			if (ownerProfile.userId != Loritta.config.ownerId) { // E ele não é o dono do bot!
				logger.info("Eu estou saindo do servidor ${guild.name} (${guild.id}) já que o dono ${ownerProfile.userId} está banido de me usar! ᕙ(⇀‸↼‶)ᕗ")
				guild.leave().complete() // Então eu irei sair daqui, me recuso a ficar em um servidor que o dono está banido! ᕙ(⇀‸↼‶)ᕗ
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
	fun isUserStillBanned(profile: LorittaProfile): Boolean {
		if (loritta.ignoreIds.contains(profile.userId)) { // Se o usuário está sendo ignorado...
			if (profile.isBanned) { // E ele ainda está banido...
				return true // Então flw galerinha
			} else {
				// Se não, vamos remover ele da lista do ignoreIds
				loritta.ignoreIds.remove(profile.userId)
				return false
			}
		}
		return false
	}
}