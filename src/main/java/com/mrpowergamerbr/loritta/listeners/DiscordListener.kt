package com.mrpowergamerbr.loritta.listeners

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.commands.vanilla.administration.BanCommand
import com.mrpowergamerbr.loritta.commands.vanilla.administration.MuteCommand
import com.mrpowergamerbr.loritta.dao.Mute
import com.mrpowergamerbr.loritta.modules.AutoroleModule
import com.mrpowergamerbr.loritta.modules.StarboardModule
import com.mrpowergamerbr.loritta.modules.WelcomeModule
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.Mutes
import com.mrpowergamerbr.loritta.userdata.PermissionsConfig
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.events.guild.GuildJoinEvent
import net.dv8tion.jda.core.events.guild.GuildLeaveEvent
import net.dv8tion.jda.core.events.guild.GuildReadyEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.core.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction

class DiscordListener(internal val loritta: Loritta) : ListenerAdapter() {
	private val logger = KotlinLogging.logger {}

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

				if (e.user.id == functions.originalAuthor && functions.onReactionAddByAuthor != null) {
					GlobalScope.launch(loritta.coroutineDispatcher) {
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

					GlobalScope.launch(loritta.coroutineDispatcher) {
						try {
							functions.onReactionRemove!!.invoke(e)
						} catch (e: Exception) {
							logger.error("Erro ao tentar processar onReactionRemove", e)
						}
					}
				}

				if (e.user.id == functions.originalAuthor && functions.onReactionRemoveByAuthor != null) {
					GlobalScope.launch(loritta.coroutineDispatcher) {
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

				updateTextChannelsTopic(event.guild, conf)

				if (conf.moderationConfig.useLorittaBansNetwork && loritta.networkBanManager.getNetworkBanEntry(event.user.id) != null) {
					val entry = loritta.networkBanManager.getNetworkBanEntry(event.user.id)!! // oof
					BanCommand.ban(
							conf,
							event.guild,
							event.guild.selfMember.user,
							com.mrpowergamerbr.loritta.utils.loritta.getLocaleById(conf.localeId),
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
							com.mrpowergamerbr.loritta.utils.loritta.getLocaleById(conf.localeId),
							event.user,
							"Sim, eu também tenho sentimentos. (Usar nomes inapropriados que ofendem outros usuários!)",
							false,
							7
					)
					return@launch
				}

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
					val locale = loritta.getLocaleById(conf.localeId)
					val muteRole = MuteCommand.getMutedRole(event.guild, loritta.getLocaleById(conf.localeId)) ?: return@launch

					event.guild.controller.addSingleRoleToMember(event.member, muteRole).queue()

					if (mute.isTemporary)
						MuteCommand.spawnRoleRemovalThread(event.guild, locale, event.user, mute.expiresAt!!) }
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
				if (event.user.id == Loritta.config.clientId) {
					return@launch
				}

				val conf = loritta.getServerConfigForGuild(event.guild.id)

				updateTextChannelsTopic(event.guild, conf)

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

	fun updateTextChannelsTopic(guild: Guild, serverConfig: ServerConfig) {
		for (textChannel in guild.textChannels) {
			if (!guild.selfMember.hasPermission(textChannel, Permission.MANAGE_CHANNEL))
				continue
			val memberCountConfig = serverConfig.getTextChannelConfig(textChannel).memberCounterConfig ?: continue
			val formattedTopic = memberCountConfig.getFormattedTopic(guild)
			textChannel.manager.setTopic(formattedTopic).queue()
		}
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
				MuteCommand.spawnRoleRemovalThread(guild, com.mrpowergamerbr.loritta.utils.loritta.getLocaleById("default"), member.user, mute.expiresAt!!)
			}
		}
	}
}