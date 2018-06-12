package com.mrpowergamerbr.loritta.listeners

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.administration.MuteCommand
import com.mrpowergamerbr.loritta.modules.AutoroleModule
import com.mrpowergamerbr.loritta.modules.StarboardModule
import com.mrpowergamerbr.loritta.modules.WelcomeModule
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.util.concurrent.ThreadPoolExecutor

class DiscordListener(internal val loritta: Loritta) : ListenerAdapter() {
	val logger by logger()

	override fun onGenericMessageReaction(e: GenericMessageReactionEvent) {
		if (e.user.isBot) // Ignorar reactions de bots
			return

		if (DebugLog.cancelAllEvents)
			return

		if ((loritta.executor as ThreadPoolExecutor).activeCount >= 512) {
			logger.error("Can't keep up! Is the server overloaded? onGenericMessageReaction")
			return
		}

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
}