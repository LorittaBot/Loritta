package com.mrpowergamerbr.loritta.listeners

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.CommandContext
import com.mrpowergamerbr.loritta.userdata.LorittaServerUserData
import com.mrpowergamerbr.loritta.utils.GuildLorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaPermission
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaUtilsKotlin
import com.mrpowergamerbr.loritta.utils.escapeMentions
import com.mrpowergamerbr.loritta.utils.f
import com.mrpowergamerbr.loritta.utils.modules.AminoConverterModule
import com.mrpowergamerbr.loritta.utils.modules.AutoroleModule
import com.mrpowergamerbr.loritta.utils.modules.InviteLinkModule
import com.mrpowergamerbr.loritta.utils.modules.SlowModeModule
import com.mrpowergamerbr.loritta.utils.modules.StarboardModule
import com.mrpowergamerbr.loritta.utils.modules.WelcomeModule
import com.mrpowergamerbr.loritta.utils.save
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Role
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.events.message.MessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.concurrent.thread

class DiscordListener(internal val loritta: Loritta) : ListenerAdapter() {
	override fun onMessageReceived(event: MessageReceivedEvent) {
		if (event.author.isBot) { // Se uma mensagem de um bot, ignore a mensagem!
			return
		}
		if (event.isFromType(ChannelType.TEXT)) { // Mensagens em canais de texto
			if (event.textChannel.isNSFW) { // lol nope, I'm outta here
				return
			}
			thread {
				try {
					val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
					val lorittaProfile = loritta.getLorittaProfileForUser(event.author.id)
					val ownerProfile = loritta.getLorittaProfileForUser(event.guild.owner.user.id)
					val locale = loritta.getLocaleById(serverConfig.localeId)

					if (ownerProfile.isBanned) { // Se o dono está banido...
						if (event.member.user.id != Loritta.config.ownerId) { // E ele não é o dono do bot!
							event.guild.leave().complete() // Então eu irei sair daqui, me recuso a ficar em um servidor que o dono está banido! ᕙ(⇀‸↼‶)ᕗ
							return@thread
						}
					}

					if (loritta.ignoreIds.contains(event.author.id)) { // Se o usuário está sendo ignorado...
						if (lorittaProfile.isBanned) { // E ele ainda está banido...
							return@thread // Então flw galerinha
						} else {
							// Se não, vamos remover ele da lista do ignoreIds
							loritta.ignoreIds.remove(event.author.id)
						}
					}

					if (event.message.rawContent.replace("!", "") == "<@297153970613387264>") {
						event.textChannel.sendMessage(locale.MENTION_RESPONSE.f(event.message.author.asMention, serverConfig.commandPrefix)).complete()
					}

					val lorittaUser = GuildLorittaUser(event.member, serverConfig, lorittaProfile)

					// ===[ SLOW MODE ]===
					if (SlowModeModule.checkForSlowMode(event, serverConfig)) {
						return@thread
					}

					// ===[ VERIFICAR INVITE LINKS ]===
					if (serverConfig.inviteBlockerConfig.isEnabled) {
						InviteLinkModule.checkForInviteLinks(event.message, event.guild, lorittaUser, serverConfig.permissionsConfig, serverConfig.inviteBlockerConfig)
					}

					// ===[ CÁLCULO DE XP ]===
					// (copyright Loritta™)

					// Primeiro iremos ver se a mensagem contém algo "interessante"
					if (event.message.strippedContent.length >= 5 && lorittaProfile.lastMessageSentHash != event.message.strippedContent.hashCode()) {
						// Primeiro iremos verificar se a mensagem é "válida"
						// 7 chars por millisegundo
						var calculatedMessageSpeed = event.message.strippedContent.toLowerCase().length.toDouble() / 7

						var diff = System.currentTimeMillis() - lorittaProfile.lastMessageSent

						if (diff > calculatedMessageSpeed * 1000) {
							var nonRepeatedCharsMessage = event.message.strippedContent.replace(Regex("(.)\\1{1,}"), "$1")

							if (nonRepeatedCharsMessage.length >= 12) {
								var gainedXp = Math.min(35, Loritta.random.nextInt(Math.max(1, nonRepeatedCharsMessage.length / 7), (Math.max(2, nonRepeatedCharsMessage.length / 4))))

								lorittaProfile.xp = lorittaProfile.xp + gainedXp
								lorittaProfile.lastMessageSentHash = event.message.strippedContent.hashCode()

								val userData = (serverConfig.userData as java.util.Map<String, LorittaServerUserData>).getOrDefault(event.member.user.id, LorittaServerUserData())
								userData.xp = userData.xp + gainedXp
								serverConfig.userData.put(event.member.user.id, userData)
								loritta save serverConfig
							}
						}
					}

					lorittaProfile.lastMessageSent = System.currentTimeMillis()
					loritta save lorittaProfile

					// ===[ CONVERTER IMAGENS DO AMINO ]===
					if (serverConfig.aminoConfig.isEnabled && serverConfig.aminoConfig.fixAminoImages)
						AminoConverterModule.convertToImage(event)

					for (eventHandler in serverConfig.nashornEventHandlers) {
						eventHandler.handleMessageReceived(event)
					}

					if (lorittaUser.hasPermission(LorittaPermission.IGNORE_COMMANDS))
						return@thread

					// Primeiro os comandos vanilla da Loritta(tm)
					loritta.commandManager.commandMap.forEach { cmd ->
						if (serverConfig.debugOptions.enableAllModules || !serverConfig.disabledCommands.contains(cmd.javaClass.simpleName)) {
							if (cmd.handle(event, serverConfig, locale, lorittaUser)) {
								return@thread
							}
						}
					}

					// E depois os comandos usando JavaScript (Nashorn)
					serverConfig.nashornCommands.forEach { cmd ->
						if (cmd.handle(event, serverConfig, locale, lorittaUser)) {
							return@thread
						}
					}

					// Executar todos os onCommandMessageReceivedFeedback
					loritta.messageContextCache.values.filter {
						it.guild == event.guild
					}.forEach {
						commandContext -> commandContext.cmd.onCommandMessageReceivedFeedback(commandContext, event, event.message)
					}

					val toLearn = event.message.strippedContent.toLowerCase().escapeMentions()
					loritta.hal.add(toLearn) // TODO: Filtrar links
				} catch (e: Exception) {
					e.printStackTrace()
					LorittaUtilsKotlin.sendStackTrace(event.message, e)
				}
			}
		} else if (event.isFromType(ChannelType.PRIVATE)) { // Mensagens em DMs
			thread {
				val serverConfig = LorittaLauncher.loritta.dummyServerConfig
				val profile = loritta.getLorittaProfileForUser(event.author.id) // Carregar perfil do usuário
				val lorittaUser = LorittaUser(event.author, serverConfig, profile)
				if (event.message.rawContent.replace("!", "").trim() == "<@297153970613387264>") {
					event.channel.sendMessage("Olá " + event.message.author.asMention + "! Em DMs você não precisa usar nenhum prefixo para falar comigo! Para ver o que eu posso fazer, use `ajuda`!").complete()
					return@thread
				}

				// Comandos vanilla da Loritta
				loritta.commandManager.commandMap.forEach{ cmd ->
					if (cmd.handle(event, serverConfig, loritta.getLocaleById("default"), lorittaUser)) {
						return@thread
					}
				}
			}
		}
	}

	override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
		if (event.author.isBot) {
			return
		}

		if (event.isFromType(ChannelType.TEXT)) { // Mensagens em canais de texto
			if (event.message.textChannel.isNSFW) { // lol nope, I'm outta here
				return
			}
			thread {
				val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
				val lorittaProfile = loritta.getLorittaProfileForUser(event.author.id)
				val lorittaUser = GuildLorittaUser(event.member, serverConfig, lorittaProfile)

				// ===[ VERIFICAR INVITE LINKS ]===
				if (serverConfig.inviteBlockerConfig.isEnabled) {
					InviteLinkModule.checkForInviteLinks(event.message, event.guild, lorittaUser, serverConfig.permissionsConfig, serverConfig.inviteBlockerConfig)
				}
			}
		}
	}

	override fun onGenericMessageReaction(e: GenericMessageReactionEvent) {
		if (e.user.isBot) {
			return
		} // Ignorar reactions de bots

		if (loritta.messageContextCache.containsKey(e.messageId)) {
			val context = LorittaLauncher.getInstance().messageContextCache[e.messageId] as CommandContext
			val t = object : Thread() {
				override fun run() {
					try {
						val msg = e.channel.getMessageById(e.messageId).complete()
						if (msg != null) {
							context.cmd.onCommandReactionFeedback(context, e, msg)
						}
					} catch (exception: Exception) {
						exception.printStackTrace()
						LorittaUtilsKotlin.sendStackTrace("[`${e.guild.name}`] **onGenericMessageReaction ${e.member.user.name}**", exception)
					}
				}
			}
			t.start()
		}

		thread {
			// TODO: Isto deveria ser feito usando a API da Loritta
			if (e.guild.id == "297732013006389252") {
				if (e.textChannel.id == "367359479877992449") {
					var role: Role? = null
					if (e.reactionEmote.name == "\uD83C\uDDE7\uD83C\uDDF7") {
						role = e.guild.getRoleById("367359104320012288")
					} else if (e.reactionEmote.name == "\uD83C\uDDFA\uD83C\uDDF8") {
						role = e.guild.getRoleById("367359247891038209")
					}

					if (role != null) {
						if (e is MessageReactionAddEvent) {
							e.guild.controller.addSingleRoleToMember(e.member, role).complete()
						} else if (e is MessageReactionRemoveEvent) {
							e.guild.controller.removeSingleRoleFromMember(e.member, role).complete()
						}
					}
				}
			}
		}

		val executor = executors.getOrPut(e.guild, { Executors.newFixedThreadPool(1) })

		executor.execute {
			try {
				val conf = loritta.getServerConfigForGuild(e.guild.id)

				// Sistema de Starboard
				if (conf.starboardConfig.isEnabled) {
					StarboardModule.handleStarboardReaction(e, conf)
				}
			} catch (exception: Exception) {
				exception.printStackTrace()
				LorittaUtilsKotlin.sendStackTrace("[`${e.guild.name}`] **Starboard ${e.member.user.name}**", exception)
			}
		}
	}

	val executors = mutableMapOf<Guild, ExecutorService>()

	override fun onGuildLeave(e: GuildLeaveEvent) {
		// Quando a Loritta sair de uma guild, automaticamente remova o ServerConfig daquele servidor
		LorittaLauncher.loritta.mongo
				.getDatabase("loritta")
				.getCollection("servers")
				.deleteMany(Filters.eq("_id", e.guild.id)) // Tchau! :(
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
		thread {
			loritta save serverConfig

			event.guild.members.forEach {
				if (!it.user.isBot && (it.hasPermission(Permission.MANAGE_SERVER) || it.hasPermission(Permission.ADMINISTRATOR))) {
					val message = loritta.getLocaleById(serverConfig.localeId)["LORITTA_ADDED_ON_SERVER", it.asMention, event.guild.name, "https://loritta.website/", "https://discord.gg/V7Kbh4z", loritta.commandManager.commandMap.size, "https://loritta.website/doar"]

					it.user.openPrivateChannel().queue({
						it.sendMessage(message).queue()
					})
				}
			}
		}
	}

	override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
		loritta.executor.execute {
			try {
				val conf = loritta.getServerConfigForGuild(event.guild.id)

				for (eventHandler in conf.nashornEventHandlers) {
					eventHandler.handleMemberJoin(event)
				}

				if (conf.autoroleConfig.isEnabled && event.guild.selfMember.hasPermission(Permission.MANAGE_ROLES)) { // Está ativado?
					AutoroleModule.giveRoles(event, conf.autoroleConfig)
				}

				if (conf.joinLeaveConfig.isEnabled) { // Está ativado?
					WelcomeModule.handleJoin(event, conf)
				}
			} catch (e: Exception) {
				e.printStackTrace()
				LorittaUtilsKotlin.sendStackTrace("[`${event.guild.name}`] **Ao entrar no servidor ${event.user.name}**", e)
			}
		}
	}

	override fun onGuildMemberLeave(event: GuildMemberLeaveEvent) {
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
				e.printStackTrace()
				LorittaUtilsKotlin.sendStackTrace("[`${event.guild.name}`] **Ao sair do servidor ${event.user.name}**", e)
			}
		}
	}
}