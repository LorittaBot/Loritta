package com.mrpowergamerbr.loritta.listeners

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.AbstractCommand
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.commands.vanilla.administration.MuteCommand
import com.mrpowergamerbr.loritta.threads.BomDiaECiaThread
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.modules.*
import kotlinx.coroutines.experimental.launch
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.core.events.message.MessageDeleteEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.util.regex.Pattern

class DiscordListener(internal val loritta: Loritta) : ListenerAdapter() {
	val logger by logger()

	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (event.author.isBot) // Se uma mensagem de um bot, ignore a mensagem!
			return

		if (DebugLog.cancelAllEvents)
			return

		if (event.isFromType(ChannelType.TEXT)) { // Mensagens em canais de texto
			loritta.executor.execute {
				try {
					if (event.member == null) {
						println("${event.author} tem a variável event.member == null no MessageReceivedEvent! (bug?)")
						println("${event.author} ainda está no servidor? ${event.guild.isMember(event.author)}")
						return@execute
					}

					val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
					val lorittaProfile = loritta.getLorittaProfileForUser(event.author.id)
					val ownerProfile = loritta.getLorittaProfileForUser(event.guild.owner.user.id)
					val locale = loritta.getLocaleById(serverConfig.localeId)
					val lorittaUser = GuildLorittaUser(event.member, serverConfig, lorittaProfile)

					lorittaProfile.isAfk = false
					lorittaProfile.afkReason = null

					if (ownerProfile.isBanned) { // Se o dono está banido...
						if (event.member.user.id != Loritta.config.ownerId) { // E ele não é o dono do bot!
							event.guild.leave().complete() // Então eu irei sair daqui, me recuso a ficar em um servidor que o dono está banido! ᕙ(⇀‸↼‶)ᕗ
							return@execute
						}
					}

					if (loritta.ignoreIds.contains(event.author.id)) { // Se o usuário está sendo ignorado...
						if (lorittaProfile.isBanned) { // E ele ainda está banido...
							return@execute // Então flw galerinha
						} else {
							// Se não, vamos remover ele da lista do ignoreIds
							loritta.ignoreIds.remove(event.author.id)
						}
					}

					if (event.message.contentRaw.replace("!", "") == "<@297153970613387264>") {
						var response = locale["MENTION_RESPONSE", event.message.author.asMention, serverConfig.commandPrefix]

						if (lorittaUser.hasPermission(LorittaPermission.IGNORE_COMMANDS)) {
							// Usuário não pode usar comandos

							// Qual é o cargo que não permite utilizar os meus comandos?
							val roles = event.member.roles.toMutableList()

							val everyone = event.member.guild.publicRole
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
								val useCommandsIn = event.guild.textChannels.firstOrNull { !serverConfig.blacklistedChannels.contains(it.id) && it.canTalk(event.member) }

								response = if (useCommandsIn != null) {
									// Canal não bloqueado!
									locale["MENTION_ResponseBlocked", event.message.author.asMention, serverConfig.commandPrefix, useCommandsIn.asMention]
								} else {
									// Nenhum canal disponível...
									locale["MENTION_ResponseBlockedNoChannels", event.message.author.asMention, serverConfig.commandPrefix]
								}
							}
						}

						event.textChannel.sendMessage("<:loritta:331179879582269451> **|** " + response).complete()
					}

					// BOM DIA & CIA
					if (serverConfig.miscellaneousConfig.enableBomDiaECia) {
						val activeTextChannelInfo = loritta.bomDiaECia.activeTextChannels.getOrDefault(event.channel.id, BomDiaECia.YudiTextChannelInfo())
						activeTextChannelInfo.lastMessageSent = System.currentTimeMillis()
						activeTextChannelInfo.users.add(event.author)
						loritta.bomDiaECia.activeTextChannels[event.channel.id] = activeTextChannelInfo
					}

					// ===[ SLOW MODE ]===
					if (SlowModeModule.checkForSlowMode(event, lorittaUser, serverConfig)) {
						return@execute
					}

					// ===[ VERIFICAR INVITE LINKS ]===
					if (serverConfig.inviteBlockerConfig.isEnabled) {
						if (InviteLinkModule.checkForInviteLinks(event.message, event.guild, lorittaUser, serverConfig.permissionsConfig, serverConfig.inviteBlockerConfig)) {
							return@execute
						}
					}

					if (event.guild.id == "297732013006389252") {
						if (DeleteNonLorittaInvitesModule.checkForInviteLinks(event.message, event.guild, lorittaUser, serverConfig.permissionsConfig, serverConfig.inviteBlockerConfig)) {
							return@execute
						}

						ServerSupportModule.checkForSupport(event, event.message)
					}

					// ===[ AUTOMOD ]===
					if (AutomodModule.handleAutomod(event, event.guild, lorittaUser, serverConfig)) {
						return@execute
					}

					// ===[ CÁLCULO DE XP ]===
					ExperienceModule.handleExperience(event, serverConfig, lorittaProfile)

					// ===[ CONVERTER IMAGENS DO AMINO ]===
					if (serverConfig.aminoConfig.fixAminoImages)
						AminoConverterModule.convertToImage(event)

					for (eventHandler in serverConfig.nashornEventHandlers) {
						eventHandler.handleMessageReceived(event)
					}

					// emotes favoritos
					event.message.emotes.forEach {
						lorittaProfile.usedEmotes.put(it.id, lorittaProfile.usedEmotes.getOrDefault(it.id, 0) + 1)
					}

					loritta save lorittaProfile

					if (lorittaUser.hasPermission(LorittaPermission.IGNORE_COMMANDS))
						return@execute

					AFKModule.handleAFK(event, locale)

					val lorittaMessageEvent = AbstractCommand.LorittaMessageEvent(
							event.author,
							event.member,
							event.message,
							event.messageId,
							event.guild,
							event.channel,
							event.textChannel
					)

					// Primeiro os comandos vanilla da Loritta(tm)
					loritta.commandManager.commandMap.filter { !serverConfig.disabledCommands.contains(it.javaClass.simpleName) }.forEach { cmd ->
						if (cmd.handle(lorittaMessageEvent, serverConfig, locale, lorittaUser)) {
							return@execute
						}
					}

					// E depois os comandos usando JavaScript (Nashorn)
					serverConfig.nashornCommands.forEach { cmd ->
						if (cmd.handle(lorittaMessageEvent, serverConfig, locale, lorittaUser)) {
							return@execute
						}
					}

					loritta.messageInteractionCache.values.forEach {
						if (it.onMessageReceived != null)
							it.onMessageReceived!!.invoke(event)

						if (it.guild == event.guild.id) {
							if (it.onResponse != null)
								it.onResponse!!.invoke(event)

							if (it.onResponseByAuthor != null) {
								if (it.originalAuthor == event.author.id)
									it.onResponseByAuthor!!.invoke(event)
							}
						}
					}

					// Executar todos os onCommandMessageReceivedFeedback
					loritta.messageContextCache.values.filter {
						!it.isPrivateChannel && it.guild == event.guild
					}.forEach {
						commandContext -> commandContext.cmd.onCommandMessageReceivedFeedback(commandContext, event, event.message)
					}

					if (event.textChannel.canTalk() && serverConfig.warnOnUnknownCommand) {
						val startsWithCommandPattern = Regex("^" + Pattern.quote(serverConfig.commandPrefix) + "[A-z0-9]+.*")

						if (event.message.contentRaw.matches(startsWithCommandPattern)) {
							val command = event.message.contentDisplay.split(" ")[0].stripCodeMarks()
							val message = event.textChannel.sendMessage("\uD83E\uDD37 **|** " + event.author.asMention + " ${locale["LORITTA_UnknownCommand", command, "${serverConfig.commandPrefix}${locale["AJUDA_CommandName"]}"]} <:blobBlush:357977010771066890>").complete()
							Thread.sleep(5000)
							message.delete().queue()
						}
					}
				} catch (e: Exception) {
					logger.error("[${event.guild.name}] Erro ao processar mensagem de ${event.author.name} (${event.author.id} - ${event.message.contentRaw}", e)
					LorittaUtilsKotlin.sendStackTrace(event.message, e)
				}
			}
		} else if (event.isFromType(ChannelType.PRIVATE)) { // Mensagens em DMs
			loritta.executor.execute {
				val serverConfig = LorittaLauncher.loritta.dummyServerConfig
				val profile = loritta.getLorittaProfileForUser(event.author.id) // Carregar perfil do usuário
				val lorittaUser = LorittaUser(event.author, serverConfig, profile)
				if (event.message.contentRaw.replace("!", "").trim() == "<@297153970613387264>") {
					event.channel.sendMessage("Olá " + event.message.author.asMention + "! Em DMs você não precisa usar nenhum prefixo para falar comigo! Para ver o que eu posso fazer, use `ajuda`!").complete()
					return@execute
				}

				val lorittaMessageEvent = AbstractCommand.LorittaMessageEvent(
						event.author,
						event.member,
						event.message,
						event.messageId,
						event.guild,
						event.channel,
						event.textChannel
				)

				// Comandos vanilla da Loritta
				loritta.commandManager.commandMap.forEach{ cmd ->
					if (cmd.handle(lorittaMessageEvent, serverConfig, loritta.getLocaleById("default"), lorittaUser)) {
						return@execute
					}
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

				// ===[ VERIFICAR INVITE LINKS ]===
				if (serverConfig.inviteBlockerConfig.isEnabled)
					InviteLinkModule.checkForInviteLinks(event.message, event.guild, lorittaUser, serverConfig.permissionsConfig, serverConfig.inviteBlockerConfig)

				val lorittaMessageEvent = AbstractCommand.LorittaMessageEvent(
						event.author,
						event.member,
						event.message,
						event.messageId,
						event.guild,
						event.channel,
						event.channel
				)

				// Primeiro os comandos vanilla da Loritta(tm)
				loritta.commandManager.commandMap.filter{ !serverConfig.disabledCommands.contains(it.javaClass.simpleName) }.forEach { cmd ->
					if (cmd.handle(lorittaMessageEvent, serverConfig, locale, lorittaUser)) {
						return@execute
					}
				}

				// E depois os comandos usando JavaScript (Nashorn)
				serverConfig.nashornCommands.forEach { cmd ->
					if (cmd.handle(lorittaMessageEvent, serverConfig, locale, lorittaUser)) {
						return@execute
					}
				}
			}
		}
	}

	override fun onMessageDelete(event: MessageDeleteEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.messageContextCache.remove(event.messageId)
		loritta.messageInteractionCache.remove(event.messageId)
	}

	override fun onGenericMessageReaction(e: GenericMessageReactionEvent) {
		if (e.user.isBot) // Ignorar reactions de bots
			return

		if (DebugLog.cancelAllEvents)
			return

		if (loritta.messageInteractionCache.containsKey(e.messageId)) {
			val functions = loritta.messageInteractionCache[e.messageId]!!

			if (e is MessageReactionAddEvent) {
				if (functions.onReactionAdd != null) {
					loritta.executor.execute {
						try {
							functions.onReactionAdd!!.invoke(e)
						} catch (e: Exception) {
							logger.error("Erro ao tentar processar onReactionAdd", e)
						}
					}
				}

				if (e.user.id == functions.originalAuthor && functions.onReactionAddByAuthor != null) {
					loritta.executor.execute {
						try {
							functions.onReactionAddByAuthor!!.invoke(e)
						} catch (e: Exception) {
							logger.error("Erro ao tentar processar onReactionAddByAuthor", e)
						}
					}
				}
			}

			if (e is MessageReactionRemoveEvent) {
				if (functions.onReactionRemove != null) {
					loritta.executor.execute {
						try {
							functions.onReactionRemove!!.invoke(e)
						} catch (e: Exception) {
							logger.error("Erro ao tentar processar onReactionRemove", e)
						}
					}
				}

				if (e.user.id == functions.originalAuthor && functions.onReactionRemoveByAuthor != null) {
					loritta.executor.execute {
						try {
							functions.onReactionRemoveByAuthor!!.invoke(e)
						} catch (e: Exception) {
							logger.error("Erro ao tentar processar onReactionRemoveByAuthor", e)
						}
					}
				}
			}
		}

		if (loritta.messageContextCache.containsKey(e.messageId)) {
			val context = LorittaLauncher.loritta.messageContextCache[e.messageId] as CommandContext
			loritta.executor.execute {
				try {
					val message = e.channel.getMessageById(e.messageId).complete()
					context.cmd.onCommandReactionFeedback(context, e, message)
				} catch (exception: Exception) {
					loritta.messageContextCache.remove(e.messageId)
					if (exception is ErrorResponseException) {
						if (exception.errorCode == 10008) // unknown channel
							return@execute
					}
					logger.error("[${e.guild.name}] onGenericMessageReaction ${e.user.name}", exception)
					LorittaUtilsKotlin.sendStackTrace("[`${e.guild.name}`] **onGenericMessageReaction ${e.user.name}**", exception)
				}
			}
		}

		loritta.executor.execute {
			if (e.isFromType(ChannelType.TEXT)) {
				try {
					val conf = loritta.getServerConfigForGuild(e.guild.id)

					// Sistema de Starboard
					if (conf.starboardConfig.isEnabled) {
						StarboardModule.handleStarboardReaction(e, conf)
					}
				} catch (exception: Exception) {
					logger.error("[${e.guild.name}] Starboard ${e.member.user.name}", exception)
					LorittaUtilsKotlin.sendStackTrace("[`${e.guild.name}`] **Starboard ${e.member.user.name}**", exception)
				}
			}
		}
	}

	override fun onGuildLeave(e: GuildLeaveEvent) {
		// Remover threads de role removal caso a Loritta tenha saido do servidor
		val toRemove = mutableListOf<String>()
		MuteCommand.roleRemovalThreads.forEach { key, value ->
			if (key.startsWith(e.guild.id)) {
				value.interrupt()
				toRemove.add(key)
			}
		}
		toRemove.forEach { MuteCommand.roleRemovalThreads.remove(it) }

		loritta.executor.execute {
			// Quando a Loritta sair de uma guild, automaticamente remova o ServerConfig daquele servidor
			loritta.serversColl.deleteOne(Filters.eq("_id", e.guild.id))
		}
	}

	override fun onGuildJoin(event: GuildJoinEvent) {
		// Vamos alterar a minha linguagem quando eu entrar em um servidor, baseando na localização dele
		val region = event.guild.region
		val regionName = region.getName()
		val serverConfig = loritta.getServerConfigForGuild(event.guild.id)

		// Portuguese
		if (regionName.startsWith("Brazil")) {
			serverConfig.localeId = "default"
		} else {
			serverConfig.localeId = "en-us"
		}

		// E depois iremos salvar a configuração do servidor
		loritta.executor.execute {
			loritta save serverConfig

			// Agora nós iremos pegar o locale do servidor
			val locale = loritta.getLocaleById(serverConfig.localeId)

			// Pegar todos os membros do servidor
			event.guild.members.forEach {
				// E, se o membro não for um bot e possui permissão de gerenciar o servidor ou permissão de administrador...
				if (!it.user.isBot && (it.hasPermission(Permission.MANAGE_SERVER) || it.hasPermission(Permission.ADMINISTRATOR))) {
					// Envie via DM uma mensagem falando sobre a Loritta!
					val message = locale["LORITTA_ADDED_ON_SERVER", it.asMention, event.guild.name, "https://loritta.website/", locale["LORITTA_SupportServerInvite"], loritta.commandManager.commandMap.size, "https://loritta.website/donate"]

					it.user.openPrivateChannel().queue({
						it.sendMessage(message).queue()
					})
				}
			}
		}
	}

	override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			try {
				val conf = loritta.getServerConfigForGuild(event.guild.id)

				for (eventHandler in conf.nashornEventHandlers) {
					eventHandler.handleMemberJoin(event)
				}

				if (event.guild.id == "297732013006389252") {
					val mutualServers = lorittaShards.getMutualGuilds(event.user)

					if (1 == mutualServers.size) {
						val textChannel = event.guild.getTextChannelById("358774895850815488")
						val locale = loritta.getLocaleById(conf.localeId)

						if (textChannel != null) {
							val embed = EmbedBuilder().apply {
								setTitle("\uD83D\uDC40 Usuário parece ser suspeito!")
								setColor(Constants.ROBLOX_RED)
								setThumbnail(event.user.effectiveAvatarUrl)
								addField("\uD83D\uDCBB " + locale["USERINFO_TAG_DO_DISCORD"], "${event.user.name}#${event.user.discriminator}", true)
								addField("\uD83D\uDCBB " + locale["USERINFO_ID_DO_DISCORD"], event.user.id, true)
								addField("\uD83C\uDF0E Servidores compartilhados (${mutualServers.size})", mutualServers.joinToString(transform = { it.name }), true)
								addField("\uD83D\uDCC5 Conta criada em", event.user.creationTime.humanize(locale), true)
							}
							textChannel.sendMessage(embed.build()).queue()
						}
					}
				}
				if (event.guild.id == "420626099257475072") {
					val mutualServers = lorittaShards.getMutualGuilds(event.user)

					if (1 == mutualServers.size) {
						val textChannel = event.guild.getTextChannelById("422103894462824468")
						val locale = loritta.getLocaleById(conf.localeId)

						if (textChannel != null) {
							val embed = EmbedBuilder().apply {
								setTitle("\uD83D\uDC40 User seems to be shady!")
								setColor(Constants.ROBLOX_RED)
								setThumbnail(event.user.effectiveAvatarUrl)
								addField("\uD83D\uDCBB " + locale["USERINFO_TAG_DO_DISCORD"], "${event.user.name}#${event.user.discriminator}", true)
								addField("\uD83D\uDCBB " + locale["USERINFO_ID_DO_DISCORD"], event.user.id, true)
								addField("\uD83C\uDF0E Mutual servers (${mutualServers.size})", mutualServers.joinToString(transform = { it.name }), true)
								addField("\uD83D\uDCC5 Account created at", event.user.creationTime.humanize(locale), true)
							}
							textChannel.sendMessage(embed.build()).queue()
						}
					}
				}

				if (conf.autoroleConfig.isEnabled && event.guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) { // Está ativado?
					AutoroleModule.giveRoles(event, conf.autoroleConfig)
				}

				if (conf.joinLeaveConfig.isEnabled) { // Está ativado?
					WelcomeModule.handleJoin(event, conf)
				}

				val userData = conf.getUserData(event.user.id)

				if (userData.isMuted) {
					var mutedRoles = event.guild.getRolesByName(loritta.getLocaleById(conf.localeId)["MUTE_ROLE_NAME"], false)
					if (mutedRoles.isEmpty())
						return@execute

					event.guild.controller.addSingleRoleToMember(event.member, mutedRoles.first()).complete()

					if (userData.temporaryMute)
						MuteCommand.spawnRoleRemovalThread(event.guild, loritta.getLocaleById(conf.localeId), conf, conf.getUserData(event.user.id))
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
		val thread = MuteCommand.roleRemovalThreads[event.guild.id + "#" + event.member.user.id]
		if (thread != null)
			thread.interrupt()
		MuteCommand.roleRemovalThreads.remove(event.guild.id + "#" + event.member.user.id)

		loritta.executor.execute {
			try {
				if (event.user.id == Loritta.config.clientId) {
					return@execute
				}

				val conf = loritta.getServerConfigForGuild(event.guild.id)

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

	override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			val config = loritta.getServerConfigForGuild(event.guild.id)

			if (!config.musicConfig.isEnabled)
				return@execute

			if ((config.musicConfig.musicGuildId ?: "").isEmpty())
				return@execute

			val voiceChannel = event.guild.getVoiceChannelById(config.musicConfig.musicGuildId) ?: return@execute

			if (voiceChannel.members.isEmpty())
				return@execute

			if (voiceChannel.members.contains(event.guild.selfMember))
				return@execute

			val mm = loritta.getGuildAudioPlayer(event.guild)

			if (mm.player.playingTrack != null && mm.player.isPaused) {
				event.guild.audioManager.openAudioConnection(voiceChannel)
				mm.player.isPaused = false
			} else {
				mm.player.isPaused = false
				LorittaUtilsKotlin.startRandomSong(event.guild, config)
			}
		}
	}

	override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			val config = loritta.getServerConfigForGuild(event.guild.id)

			if (!config.musicConfig.isEnabled)
				return@execute

			if ((config.musicConfig.musicGuildId ?: "").isEmpty())
				return@execute

			val voiceChannel = event.guild.getVoiceChannelById(config.musicConfig.musicGuildId) ?: return@execute

			if (voiceChannel.members.any { !it.user.isBot && (!it.voiceState.isDeafened && !it.voiceState.isGuildDeafened) })
				return@execute

			val mm = loritta.getGuildAudioPlayer(event.guild)

			if (mm.player.playingTrack != null) {
				mm.player.isPaused = true // Pausar música caso todos os usuários saiam
			}

			event.guild.audioManager.closeAudioConnection() // E desconectar do canal de voz
		}
	}
}