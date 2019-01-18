package com.mrpowergamerbr.loritta.listeners

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.modules.Modules
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.eventlog.EventLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.events.message.MessageDeleteEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.core.events.message.priv.PrivateMessageReceivedEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

class MessageListener(val loritta: Loritta) : ListenerAdapter() {
	companion object {
		private val logger = KotlinLogging.logger {}
		val MESSAGE_RECEIVED_MODULES = mutableListOf(
				Modules.SLOW_MODE,
				Modules.AUTOMOD,
				Modules.INVITE_LINK,
				Modules.SERVER_SUPPORT,
				Modules.EXPERIENCE,
				Modules.AMINO_CONVERTER,
				Modules.AFK,
				Modules.BOM_DIA_E_CIA,
				Modules.QUIRKY,
				Modules.THANK_YOU_LORI
		)

		val MESSAGE_EDITED_MODULES = mutableListOf(
				Modules.INVITE_LINK,
				Modules.AUTOMOD,
				Modules.SERVER_SUPPORT
		)
	}

	override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
		if (event.author.isBot) // Se uma mensagem de um bot, ignore a mensagem!
			return

		if (DebugLog.cancelAllEvents)
			return

		GlobalScope.launch(loritta.coroutineDispatcher) {
			try {
				val member = event.member
				if (member == null) { // Isto parece estúpido, mas realmente funciona
					logger.warn { "${event.author} saiu do servidor ${event.guild.id} antes de eu poder processar a mensagem"}
					return@launch
				}

				val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
				val lorittaProfile = loritta.getOrCreateLorittaProfile(event.author.idLong)
				val ownerProfile = loritta.getLorittaProfile(event.guild.owner.user.idLong)
				val locale = loritta.getLocaleById(serverConfig.localeId)
				val legacyLocale = loritta.getLegacyLocaleById(serverConfig.localeId)
				val lorittaUser = GuildLorittaUser(member, serverConfig, lorittaProfile)

				if (lorittaProfile.isAfk) {
					transaction(Databases.loritta) {
						lorittaProfile.isAfk = false
						lorittaProfile.afkReason = null
					}
				}

				if (ownerProfile != null && isOwnerBanned(ownerProfile, event.guild))
					return@launch

				if (isGuildBanned(event.guild))
					return@launch

				EventLog.onMessageReceived(serverConfig, event.message)

				if (isMentioningMe(event.message))
					if (chance(25.0) && serverConfig.miscellaneousConfig.enableQuirky && event.member.hasPermission(Permission.MESSAGE_ADD_REACTION, Permission.MESSAGE_EXT_EMOJI))
						event.message.addReaction("smol_lori_putassa_ping:397748526362132483").queue()

				if (isMentioningOnlyMe(event.message.contentRaw)) {
					var response = legacyLocale["MENTION_RESPONSE", member.asMention, serverConfig.commandPrefix]

					if (lorittaUser.hasPermission(LorittaPermission.IGNORE_COMMANDS)) {
						// Usuário não pode usar comandos

						// Qual é o cargo que não permite utilizar os meus comandos?
						val roles = member.roles.toMutableList()

						val everyone = member.guild.publicRole
						if (everyone != null) {
							roles.add(everyone)
						}

						roles.sortByDescending { it.position }

						var ignoringCommandsRole: Role? = null
						for (role in roles) {
							val permissionRole = serverConfig.permissionsConfig.roles.getOrDefault(role.id, PermissionsConfig.PermissionRole())
							if (permissionRole.permissions.contains(LorittaPermission.IGNORE_COMMANDS)) {
								ignoringCommandsRole = role
								break
							}
						}

						if (ignoringCommandsRole == event.guild.publicRole)
							response = legacyLocale["MENTION_ResponseEveryoneBlocked", event.message.author.asMention, serverConfig.commandPrefix]
						else
							response = legacyLocale["MENTION_ResponseRoleBlocked", event.message.author.asMention, serverConfig.commandPrefix, ignoringCommandsRole?.asMention]
					} else {
						if (serverConfig.blacklistedChannels.contains(event.channel.id) && !lorittaUser.hasPermission(LorittaPermission.BYPASS_COMMAND_BLACKLIST)) {
							// Vamos pegar um canal que seja possível usar comandos
							val useCommandsIn = event.guild.textChannels.firstOrNull { !serverConfig.blacklistedChannels.contains(it.id) && it.canTalk(member) }

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

				for (module in MESSAGE_RECEIVED_MODULES) {
					if (module.matches(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, legacyLocale) && module.handle(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, legacyLocale))
						return@launch
				}

				for (eventHandler in serverConfig.nashornEventHandlers)
					eventHandler.handleMessageReceived(event, serverConfig)

				if (lorittaUser.hasPermission(LorittaPermission.IGNORE_COMMANDS))
					return@launch

				if (isUserStillBanned(lorittaProfile))
					return@launch

				// Executar comandos
				if (loritta.legacyCommandManager.matches(lorittaMessageEvent, serverConfig, locale, legacyLocale, lorittaUser))
					return@launch

				if (loritta.commandManager.dispatch(lorittaMessageEvent, serverConfig, locale, legacyLocale, lorittaUser))
					return@launch

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

				if (event.channel.canTalk() && serverConfig.warnOnUnknownCommand) {
					val startsWithCommandPattern = Regex("^" + Pattern.quote(serverConfig.commandPrefix) + "[A-z0-9]+.*")

					if (event.message.contentRaw.matches(startsWithCommandPattern)) {
						val command = event.message.contentDisplay.split(" ")[0].stripCodeMarks()
						val message = event.channel.sendMessage("\uD83E\uDD37 **|** ${event.author.asMention} ${legacyLocale["LORITTA_UnknownCommand", command, "${serverConfig.commandPrefix}${legacyLocale["AJUDA_CommandName"]}"]} ${Emotes.LORI_OWO}").queue {
							it.delete().queueAfter(5000, TimeUnit.MILLISECONDS)
						}
					}
				}
			} catch (e: Exception) {
				logger.error("[${event.guild.name}] Erro ao processar mensagem de ${event.author.name} (${event.author.id} - ${event.message.contentRaw}", e)
				LorittaUtilsKotlin.sendStackTrace(event.message, e)
			}
		}
	}

	override fun onPrivateMessageReceived(event: PrivateMessageReceivedEvent) {
		GlobalScope.launch(loritta.coroutineDispatcher) {
			val serverConfig = LorittaLauncher.loritta.dummyServerConfig
			val profile = loritta.getOrCreateLorittaProfile(event.author.idLong) // Carregar perfil do usuário
			val lorittaUser = LorittaUser(event.author, serverConfig, profile)
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

			// Comandos vanilla da Loritta
			if (loritta.legacyCommandManager.matches(lorittaMessageEvent, serverConfig, locale, legacyLocale, lorittaUser))
				return@launch

			if (loritta.commandManager.dispatch(lorittaMessageEvent, serverConfig, locale, legacyLocale, lorittaUser))
				return@launch
		}
	}

	override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
		if (event.author.isBot)
			return

		if (DebugLog.cancelAllEvents)
			return

		if (event.channel.type == ChannelType.TEXT) { // Mensagens em canais de texto
			GlobalScope.launch(loritta.coroutineDispatcher) {
				val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
				val lorittaProfile = loritta.getOrCreateLorittaProfile(event.author.idLong)
				val legacyLocale = loritta.getLegacyLocaleById(serverConfig.localeId)
				val locale = loritta.getLocaleById(serverConfig.localeId)
				val lorittaUser = GuildLorittaUser(event.member, serverConfig, lorittaProfile)

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

				for (module in MESSAGE_EDITED_MODULES) {
					if (module.matches(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, legacyLocale) && module.handle(lorittaMessageEvent, lorittaUser, lorittaProfile, serverConfig, legacyLocale))
						return@launch
				}

				// Executar comandos
				if (loritta.legacyCommandManager.matches(lorittaMessageEvent, serverConfig, locale, legacyLocale, lorittaUser))
					return@launch

				if (loritta.commandManager.dispatch(lorittaMessageEvent, serverConfig, locale, legacyLocale, lorittaUser)) {
					return@launch
				}
			}
		}
	}

	override fun onMessageDelete(event: MessageDeleteEvent) {
		loritta.messageInteractionCache.remove(event.messageIdLong)
	}

	/**
	 * Checks if the message contains only a mention for me
	 *
	 * @param contentRaw the raw content of the message
	 * @returns if the message is mentioning only me
	 */
	fun isMentioningOnlyMe(contentRaw: String): Boolean = contentRaw.replace("!", "").trim() == "<@${Loritta.config.clientId}>"

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
			if (ownerProfile.userId != Loritta.config.ownerId.toLong()) { // E ele não é o dono do bot!
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
		if (loritta.blacklistedServers.any { it.key == guild.id }) { // Se o servidor está banido...
			if (guild.owner.user.id != Loritta.config.ownerId) { // E ele não é o dono do bot!
				logger.info("Eu estou saindo do servidor ${guild.name} (${guild.id}) já que o servidor está banido de me usar! ᕙ(⇀‸↼‶)ᕗ")
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
				return true // Então flw galerinha
			} else {
				// Se não, vamos remover ele da lista do ignoreIds
				loritta.ignoreIds.remove(profile.userId)
				return false
			}
		}
		return false
	}

	fun handleQuirkyStuff() {

	}
}