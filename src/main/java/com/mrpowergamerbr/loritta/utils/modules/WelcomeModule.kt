package com.mrpowergamerbr.loritta.utils.modules

import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.substringIfNeeded
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.audit.ActionType
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.exceptions.ErrorResponseException

object WelcomeModule {
	fun handleJoin(event: GuildMemberJoinEvent, serverConfig: ServerConfig) {
		val joinLeaveConfig = serverConfig.joinLeaveConfig
		if (joinLeaveConfig.tellOnJoin && joinLeaveConfig.joinMessage.isNotEmpty()) { // E o sistema de avisar ao entrar está ativado?
			val guild = event.guild

			if (joinLeaveConfig.canalJoinId != null) {
				val textChannel = guild.getTextChannelById(joinLeaveConfig.canalJoinId)

				if (textChannel != null) {
					if (textChannel.canTalk()) {
						val msg = joinLeaveConfig.joinMessage
						if (msg.isNotEmpty())
							textChannel.sendMessage(MessageUtils.generateMessage(msg, event, guild)).complete()
					} else {
						LorittaUtils.warnOwnerNoPermission(guild, textChannel, serverConfig)
					}
				}
			}
		}

		if (joinLeaveConfig.tellOnPrivate && joinLeaveConfig.joinPrivateMessage.isNotEmpty()) { // Talvez o sistema de avisar no privado esteja ativado!
			if (!event.user.isBot) { // Mas antes precisamos verificar se o usuário que entrou não é um bot!
				val msg = joinLeaveConfig.joinPrivateMessage
				try {
					if (msg.isNotEmpty())
						event.user.openPrivateChannel().complete().sendMessage(MessageUtils.generateMessage(msg, event, event.guild)).complete() // Pronto!
				} catch (e: ErrorResponseException) {
					if (e.errorResponse.code != 50007) { // Usuário tem as DMs desativadas
						throw e
					}
				}
			}
		}
	}

	fun handleLeave(event: GuildMemberLeaveEvent, serverConfig: ServerConfig) {
		Thread.sleep(500) // esperar 0.5ms antes de avisar
		val joinLeaveConfig = serverConfig.joinLeaveConfig
		if (joinLeaveConfig.tellOnLeave && joinLeaveConfig.leaveMessage.isNotEmpty()) {
			val guild = event.guild

			if (joinLeaveConfig.canalLeaveId != null) {
				val textChannel = guild.getTextChannelById(joinLeaveConfig.canalLeaveId)

				if (textChannel != null) {
					if (textChannel.canTalk()) {
						var msg = joinLeaveConfig.leaveMessage
						val customTokens = mutableMapOf<String, String>()

						if (joinLeaveConfig.tellOnBan) {
							// Para a mensagem de ban nós precisamos ter a permissão de banir membros
							if (event.guild.selfMember.hasPermission(Permission.BAN_MEMBERS)) {
								val banList = guild.banList.complete()
								if (banList.firstOrNull { it.user == event.user } != null) {
									if (joinLeaveConfig.banMessage.isNotEmpty()) {
										msg = joinLeaveConfig.banMessage
									}
								}
							}
						}

						if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
							val auditLogs = guild.auditLogs.complete()
							if (auditLogs.isNotEmpty()) {
								val entry = guild.auditLogs.complete().first()

								if (entry.targetId == event.user.id) {
									if (joinLeaveConfig.tellOnKick && entry.type == ActionType.KICK) {
										msg = joinLeaveConfig.kickMessage
										customTokens["reason"] = entry.reason ?: "\uD83E\uDD37"
										customTokens["@staff"] = entry.user.asMention
										customTokens["staff"] = entry.user.name
									}
									if (entry.type == ActionType.BAN) {
										customTokens["reason"] = entry.reason ?: "\uD83E\uDD37"
										customTokens["@staff"] = entry.user.asMention
										customTokens["staff"] = entry.user.name
									}
								}
							}
						}

						if (msg.isNotEmpty())
							textChannel.sendMessage(MessageUtils.generateMessage(msg, event, guild, customTokens)).complete()
					} else {
						LorittaUtils.warnOwnerNoPermission(guild, textChannel, serverConfig)
					}
				}
			}
		}
	}
}