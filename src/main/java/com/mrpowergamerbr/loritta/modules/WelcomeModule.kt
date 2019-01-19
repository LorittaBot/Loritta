package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher.loritta
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.Emotes
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.audit.ActionType
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberLeaveEvent
import org.apache.commons.io.IOUtils
import java.nio.charset.Charset
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

object WelcomeModule {
	private val logger = KotlinLogging.logger {}

	val leftMembersCache = Caffeine.newBuilder()
			.expireAfterAccess(15, TimeUnit.SECONDS)
			.removalListener { k1: Long?, v1: CopyOnWriteArrayList<User>?, removalCause ->
				if (k1 != null && v1 != null) {
					logger.info("Removendo left members cache de ${k1}... ${v1.size} membros tinham saído durante este período")

					if (v1.size > 20) {
						logger.info("Mais de 20 membros sairam em menos de 15 segundos em ${k1}! Que triste, né? Vamos enviar um arquivo com todos que sairam!")

						val joinLeaveConfig = loritta.getServerConfigForGuild(v1.toString()).joinLeaveConfig
						if (joinLeaveConfig.tellOnLeave && joinLeaveConfig.leaveMessage.isNotEmpty()) {
							val guild = lorittaShards.getGuildById(k1) ?: return@removalListener

							if (joinLeaveConfig.canalLeaveId != null) {
								val textChannel = guild.getTextChannelById(joinLeaveConfig.canalLeaveId)

								if (textChannel != null) {
									if (textChannel.canTalk()) {
										if (guild.selfMember.hasPermission(Permission.MESSAGE_ATTACH_FILES)) {
											val lines = mutableListOf<String>()
											for (user in v1) {
												lines.add("${user.name}#${user.discriminator} - (${user.id})")
											}
											val targetStream = IOUtils.toInputStream(lines.joinToString("\n"), Charset.defaultCharset())
											textChannel.sendFile(targetStream, "left-users.log", MessageBuilder().setContent("Quanta gente saindo! Para não encher o canal de mensagens, aqui está a lista de todos que sairam ${Emotes.LORI_OWO}").build()).queue()
											logger.info("Enviado arquivo de texto em $k1 com todas as pessoas que sairam, yay!")
										}
									}
								}
							}
						}
					}
				}
			}
			.build<Long, CopyOnWriteArrayList<User>>()

	suspend fun handleJoin(event: GuildMemberJoinEvent, serverConfig: MongoServerConfig) {
		if (Loritta.config.ghostIds.contains(event.user.id)) // Ignorar ID do RevengeTakedown
			return

		val joinLeaveConfig = serverConfig.joinLeaveConfig
		val tokens = mapOf(
				"humanized-date" to event.member.joinDate.humanize(loritta.getLegacyLocaleById(serverConfig.localeId))
		)

		if (joinLeaveConfig.tellOnJoin && joinLeaveConfig.joinMessage.isNotEmpty()) { // E o sistema de avisar ao entrar está ativado?
			val guild = event.guild

			if (joinLeaveConfig.canalJoinId != null) {
				val textChannel = guild.getTextChannelById(joinLeaveConfig.canalJoinId)

				if (textChannel != null) {
					if (textChannel.canTalk()) {
						val msg = joinLeaveConfig.joinMessage
						if (msg.isNotEmpty() && event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
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

		if (!event.user.isBot && joinLeaveConfig.tellOnPrivate && joinLeaveConfig.joinPrivateMessage.isNotEmpty()) { // Talvez o sistema de avisar no privado esteja ativado!
			val msg = joinLeaveConfig.joinPrivateMessage

			if (msg.isNotEmpty() && event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
				event.user.openPrivateChannel().queue {
					it.sendMessage(MessageUtils.generateMessage(msg, listOf(event.guild, event.member), event.guild, tokens)).queue() // Pronto!
				}
			}
		}
	}

	suspend fun handleLeave(event: GuildMemberLeaveEvent, serverConfig: MongoServerConfig) {
		if (Loritta.config.ghostIds.contains(event.user.id)) // Ignorar ID do RevengeTakedown
			return

		delay(500) // esperar 0.5ms antes de avisar
		val joinLeaveConfig = serverConfig.joinLeaveConfig
		if (joinLeaveConfig.tellOnLeave && joinLeaveConfig.leaveMessage.isNotEmpty()) {
			val guild = event.guild

			val list = leftMembersCache.getIfPresent(event.guild.idLong) ?: CopyOnWriteArrayList()
			list.add(event.user)
			leftMembersCache.put(event.guild.idLong, list)

			if (list.size > 20)
				return

			if (joinLeaveConfig.canalLeaveId != null) {
				val textChannel = guild.getTextChannelById(joinLeaveConfig.canalLeaveId)

				if (textChannel != null) {
					if (textChannel.canTalk()) {
						var msg = joinLeaveConfig.leaveMessage
						val customTokens = mutableMapOf<String, String>()

						if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
							val auditLogs = guild.auditLogs.await()
							if (auditLogs.isNotEmpty()) {
								val entry = auditLogs.firstOrNull { it.targetId == event.user.id }

								if (entry != null) {
									if (joinLeaveConfig.tellOnKick && entry.type == ActionType.KICK) {
										if (joinLeaveConfig.kickMessage.isNotEmpty()) {
											msg = joinLeaveConfig.kickMessage
											customTokens["reason"] = entry.reason ?: "\uD83E\uDD37"
											customTokens["@staff"] = entry.user?.asMention ?: "???"
											customTokens["staff"] = entry.user?.name ?: "???"
										}
									}
									if (entry.type == ActionType.BAN) {
										if (joinLeaveConfig.banMessage.isNotEmpty()) {
											msg = joinLeaveConfig.banMessage
											customTokens["reason"] = entry.reason ?: "\uD83E\uDD37"
											customTokens["@staff"] = entry.user?.asMention ?: "???"
											customTokens["staff"] = entry.user?.name ?: "???"
										}
									}
								}
							}
						}

						if (msg.isNotEmpty()) {
							textChannel.sendMessage(MessageUtils.generateMessage(msg, listOf(event.guild, event.member), guild, customTokens)).queue {
								if (serverConfig.joinLeaveConfig.deleteLeaveMessagesAfter != null)
									it.delete().queueAfter(serverConfig.joinLeaveConfig.deleteLeaveMessagesAfter!!, TimeUnit.SECONDS)
							}
						}
					} else {
						LorittaUtils.warnOwnerNoPermission(guild, textChannel, serverConfig)
					}
				}
			}
		}
	}
}