package com.mrpowergamerbr.loritta.modules

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher.loritta
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.audit.ActionType
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import net.dv8tion.jda.core.exceptions.ErrorResponseException
import java.util.concurrent.TimeUnit

object WelcomeModule {
	fun handleJoin(event: GuildMemberJoinEvent, serverConfig: ServerConfig) {
		if (Loritta.config.ghostIds.contains(event.user.id)) // Ignorar ID do RevengeTakedown
			return

		val joinLeaveConfig = serverConfig.joinLeaveConfig
		val tokens = mapOf(
				"humanized-date" to event.member.joinDate.humanize(loritta.getLocaleById(serverConfig.localeId))
		)

		if (joinLeaveConfig.tellOnJoin && joinLeaveConfig.joinMessage.isNotEmpty()) { // E o sistema de avisar ao entrar está ativado?
			val guild = event.guild

			if (joinLeaveConfig.canalJoinId != null) {
				val textChannel = guild.getTextChannelById(joinLeaveConfig.canalJoinId)

				if (textChannel != null) {
					if (textChannel.canTalk()) {
						val msg = joinLeaveConfig.joinMessage
						if (msg.isNotEmpty()) {
							textChannel.sendMessage(MessageUtils.generateMessage(msg, listOf(guild, event.member), guild, tokens)).queue {
								if (serverConfig.joinLeaveConfig.deleteJoinMessagesAfter != null)
									it.delete().queueAfter(serverConfig.joinLeaveConfig.deleteJoinMessagesAfter!!, TimeUnit.SECONDS)
							}
						}
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
						event.user.openPrivateChannel().queue {
							it.sendMessage(MessageUtils.generateMessage(msg, listOf(event.guild, event.member), event.guild, tokens)).queue() // Pronto!
						}
				} catch (e: ErrorResponseException) {
					if (e.errorResponse.code != 50007) { // Usuário tem as DMs desativadas
						throw e
					}
				}
			}
		}
	}

	fun handleLeave(event: GuildMemberLeaveEvent, serverConfig: ServerConfig) {
		if (Loritta.config.ghostIds.contains(event.user.id)) // Ignorar ID do RevengeTakedown
			return

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

						val callback: (String, Map<String, String>) -> Unit = { msg, customTokens ->
							textChannel.sendMessage(MessageUtils.generateMessage(msg, listOf(event.guild, event.member), guild, customTokens)).queue {
								if (serverConfig.joinLeaveConfig.deleteLeaveMessagesAfter != null)
									it.delete().queueAfter(serverConfig.joinLeaveConfig.deleteLeaveMessagesAfter!!, TimeUnit.SECONDS)
							}
						}

						if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
							guild.auditLogs.queue { auditLogs ->
								if (auditLogs.isNotEmpty()) {
									val entry = guild.auditLogs.queue { entries ->
										val entry = entries.firstOrNull { it.targetId == event.user.id }

										if (entry != null) {
											if (entry.targetId == event.user.id) {
												if (joinLeaveConfig.tellOnKick && entry.type == ActionType.KICK) {
													if (joinLeaveConfig.kickMessage.isNotEmpty()) {
														msg = joinLeaveConfig.kickMessage
														customTokens["reason"] = entry.reason ?: "\uD83E\uDD37"
														customTokens["@staff"] = entry.user?.asMention ?: "???"
														customTokens["staff"] = entry.user?.name ?: "???"
														callback.invoke(msg, customTokens)
														return@queue
													}
												}
												if (entry.type == ActionType.BAN) {
													if (joinLeaveConfig.banMessage.isNotEmpty()) {
														msg = joinLeaveConfig.banMessage
														customTokens["reason"] = entry.reason ?: "\uD83E\uDD37"
														customTokens["@staff"] = entry.user?.asMention ?: "???"
														customTokens["staff"] = entry.user?.name ?: "???"
														callback.invoke(msg, customTokens)
														return@queue
													}
												}
											}
										}
										callback.invoke(msg, customTokens)
									}
								}
							}
							return
						}

						if (msg.isNotEmpty())
							callback.invoke(msg, customTokens)
					} else {
						LorittaUtils.warnOwnerNoPermission(guild, textChannel, serverConfig)
					}
				}
			}
		}
	}
}