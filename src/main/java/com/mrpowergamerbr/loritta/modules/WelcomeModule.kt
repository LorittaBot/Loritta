package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.LorittaLauncher.loritta
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.getTextChannelByNullableId
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.lorittaShards
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.audit.ActionType
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberLeaveEvent
import net.perfectdreams.loritta.utils.Emotes
import org.apache.commons.io.IOUtils
import java.nio.charset.Charset
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.TimeUnit

object WelcomeModule {
	private val logger = KotlinLogging.logger {}

	val joinMembersCache = Caffeine.newBuilder()
			.expireAfterAccess(15, TimeUnit.SECONDS)
			.removalListener { k1: Long?, v1: CopyOnWriteArrayList<User>?, removalCause ->
				if (k1 != null && v1 != null) {
					logger.info("Removendo join members cache de ${k1}... ${v1.size} membros tinham saído durante este período")

					if (v1.size > 20) {
						logger.info("Mais de 20 membros entraram em menos de 15 segundos em ${k1}! Que triste, né? Vamos enviar um arquivo com todos que sairam!")

						val serverConfig = loritta.getServerConfigForGuild(v1.toString())
						val joinLeaveConfig = serverConfig.joinLeaveConfig

						if (joinLeaveConfig.tellOnJoin && joinLeaveConfig.joinMessage.isNotEmpty()) {
							val guild = lorittaShards.getGuildById(k1) ?: return@removalListener

							if (joinLeaveConfig.canalJoinId != null) {
								val textChannel = guild.getTextChannelByNullableId(joinLeaveConfig.canalJoinId)

								if (textChannel != null) {
									if (textChannel.canTalk()) {
										if (guild.selfMember.hasPermission(Permission.MESSAGE_ATTACH_FILES)) {
											val lines = mutableListOf<String>()
											for (user in v1) {
												lines.add("${user.name}#${user.discriminator} - (${user.id})")
											}
											val targetStream = IOUtils.toInputStream(lines.joinToString("\n"), Charset.defaultCharset())

											val locale = loritta.getLocaleById(serverConfig.localeId)

											textChannel.sendMessage(MessageBuilder().setContent(locale["module.welcomer.tooManyUsersJoining", Emotes.LORI_OWO]).build()).addFile(targetStream, "join-users.log").queue()
											logger.info("Enviado arquivo de texto em $k1 com todas as pessoas que entraram, yay!")
										}
									}
								}
							}
						}
					}
				}
			}
			.build<Long, CopyOnWriteArrayList<User>>()
	val leftMembersCache = Caffeine.newBuilder()
			.expireAfterAccess(15, TimeUnit.SECONDS)
			.removalListener { k1: Long?, v1: CopyOnWriteArrayList<User>?, removalCause ->
				if (k1 != null && v1 != null) {
					logger.info("Removendo left members cache de ${k1}... ${v1.size} membros tinham saído durante este período")

					if (v1.size > 20) {
						logger.info("Mais de 20 membros sairam em menos de 15 segundos em ${k1}! Que triste, né? Vamos enviar um arquivo com todos que sairam!")

						val serverConfig = loritta.getServerConfigForGuild(v1.toString())
						val joinLeaveConfig = serverConfig.joinLeaveConfig

						if (joinLeaveConfig.tellOnLeave && joinLeaveConfig.leaveMessage.isNotEmpty()) {
							val guild = lorittaShards.getGuildById(k1) ?: return@removalListener

							if (joinLeaveConfig.canalLeaveId != null) {
								val textChannel = guild.getTextChannelByNullableId(joinLeaveConfig.canalLeaveId)

								if (textChannel != null) {
									if (textChannel.canTalk()) {
										if (guild.selfMember.hasPermission(Permission.MESSAGE_ATTACH_FILES)) {
											val lines = mutableListOf<String>()
											for (user in v1) {
												lines.add("${user.name}#${user.discriminator} - (${user.id})")
											}
											val targetStream = IOUtils.toInputStream(lines.joinToString("\n"), Charset.defaultCharset())

											val locale = loritta.getLocaleById(serverConfig.localeId)
											
											textChannel.sendMessage(MessageBuilder().setContent(locale["module.welcomer.tooManyUsersLeaving", Emotes.LORI_OWO]).build()).addFile(targetStream, "left-users.log").queue()
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
		if (loritta.discordConfig.ghostIds.contains(event.user.id)) // Ignorar ID do RevengeTakedown
			return

		val joinLeaveConfig = serverConfig.joinLeaveConfig
		val tokens = mapOf(
				"humanized-date" to event.member.timeJoined.humanize(loritta.getLegacyLocaleById(serverConfig.localeId))
		)

		if (joinLeaveConfig.tellOnJoin && joinLeaveConfig.joinMessage.isNotEmpty()) { // E o sistema de avisar ao entrar está ativado?
			val guild = event.guild

			val list = joinMembersCache.getIfPresent(event.guild.idLong) ?: CopyOnWriteArrayList()
			list.add(event.user)
			joinMembersCache.put(event.guild.idLong, list)

			if (list.size > 20)
				return

			if (joinLeaveConfig.canalJoinId != null) {
				val textChannel = guild.getTextChannelByNullableId(joinLeaveConfig.canalJoinId)

				if (textChannel != null) {
					if (textChannel.canTalk()) {
						val msg = joinLeaveConfig.joinMessage
						if (msg.isNotEmpty() && event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
							textChannel.sendMessage(MessageUtils.generateMessage(msg, listOf(guild, event.member), guild, tokens)!!).queue {
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
					it.sendMessage(MessageUtils.generateMessage(msg, listOf(event.guild, event.member), event.guild, tokens)!!).queue() // Pronto!
				}
			}
		}
	}

	suspend fun handleLeave(event: GuildMemberLeaveEvent, serverConfig: MongoServerConfig) {
		if (loritta.discordConfig.ghostIds.contains(event.user.id)) // Ignorar ID do RevengeTakedown
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
				val textChannel = guild.getTextChannelByNullableId(joinLeaveConfig.canalLeaveId)

				if (textChannel != null) {
					if (textChannel.canTalk()) {
						var msg = joinLeaveConfig.leaveMessage
						val customTokens = mutableMapOf<String, String>()

						if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
							val auditLogs = guild.retrieveAuditLogs().await()
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
							textChannel.sendMessage(MessageUtils.generateMessage(msg, listOf(event.guild, event.member), guild, customTokens)!!).queue {
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