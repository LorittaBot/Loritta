package com.mrpowergamerbr.loritta.utils.modules

import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUtils
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
						val msg = LorittaUtils.replaceTokens(joinLeaveConfig.joinMessage, event)
						if (msg.isNotEmpty())
							textChannel.sendMessage(msg.substringIfNeeded()).complete()
					} else {
						LorittaUtils.warnOwnerNoPermission(guild, textChannel, serverConfig)
					}
				}
			}
		}

		if (joinLeaveConfig.tellOnPrivate && joinLeaveConfig.joinPrivateMessage.isNotEmpty()) { // Talvez o sistema de avisar no privado esteja ativado!
			if (!event.user.isBot) { // Mas antes precisamos verificar se o usuário que entrou não é um bot!
				val msg = LorittaUtils.replaceTokens(joinLeaveConfig.joinPrivateMessage, event)
				try {
					if (msg.isNotEmpty())
						event.user.openPrivateChannel().complete().sendMessage(msg.substringIfNeeded()).complete() // Pronto!
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
						var msg = LorittaUtils.replaceTokens(joinLeaveConfig.leaveMessage, event)

						if (joinLeaveConfig.tellOnBan) {
							// Para a mensagem de ban nós precisamos ter a permissão de banir membros
							if (event.guild.selfMember.hasPermission(Permission.BAN_MEMBERS)) {
								val banList = guild.bans.complete()
								if (banList.contains(event.user)) {

									if (joinLeaveConfig.banMessage.isNotEmpty()) {
										msg = LorittaUtils.replaceTokens(joinLeaveConfig.banMessage, event)
									}
								}
							}
						}

						if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
							val entry = guild.auditLogs.complete().first()

							if (entry.targetId == event.user.id) {
								if (joinLeaveConfig.tellOnKick && entry.type == ActionType.KICK) {
									msg = LorittaUtils.replaceTokens(joinLeaveConfig.kickMessage, event)
									msg = msg.replace("{reason}", entry.reason ?: "\uD83E\uDD37")
									msg = msg.replace("{@staff}", entry.user.asMention)
									msg = msg.replace("{staff}", entry.user.name)
								}
								if (entry.type == ActionType.BAN) {
									msg = msg.replace("{reason}", entry.reason ?: "\uD83E\uDD37")
									msg = msg.replace("{@staff}", entry.user.asMention)
									msg = msg.replace("{staff}", entry.user.name)
								}
							}
						}

						if (msg.isNotEmpty())
							textChannel.sendMessage(msg.substringIfNeeded()).complete()
					} else {
						LorittaUtils.warnOwnerNoPermission(guild, textChannel, serverConfig)
					}
				}
			}
		}
	}
}