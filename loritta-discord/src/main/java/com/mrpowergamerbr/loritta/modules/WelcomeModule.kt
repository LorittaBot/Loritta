package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.LorittaLauncher.loritta
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.listeners.EventLogListener
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.MessageUtils
import com.mrpowergamerbr.loritta.utils.extensions.getTextChannelByNullableId
import com.mrpowergamerbr.loritta.utils.extensions.humanize
import com.mrpowergamerbr.loritta.utils.lorittaShards
import mu.KotlinLogging
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
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
					logger.info("Removendo join members cache de $k1... ${v1.size} membros tinham saído durante este período")

					if (v1.size > 20) {
						logger.info("Mais de 20 membros entraram em menos de 15 segundos em $k1! Que triste, né? Vamos enviar um arquivo com todos que sairam!")

						val legacyServerConfig = loritta.getServerConfigForGuild(k1.toString())
						val serverConfig = loritta.getOrCreateServerConfig(k1)
						val joinLeaveConfig = legacyServerConfig.joinLeaveConfig

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
					logger.info("Removendo left members cache de $k1... ${v1.size} membros tinham saído durante este período")

					if (v1.size > 20) {
						logger.info("Mais de 20 membros sairam em menos de 15 segundos em $k1! Que triste, né? Vamos enviar um arquivo com todos que sairam!")

						val legacyServerConfig = loritta.getServerConfigForGuild(k1.toString())
						val serverConfig = loritta.getOrCreateServerConfig(k1)
						val joinLeaveConfig = legacyServerConfig.joinLeaveConfig

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

	suspend fun handleJoin(event: GuildMemberJoinEvent, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig) {
		val joinLeaveConfig = legacyServerConfig.joinLeaveConfig
		val tokens = mapOf(
				"humanized-date" to event.member.timeJoined.humanize(loritta.getLegacyLocaleById(serverConfig.localeId))
		)

		logger.trace { "Member = ${event.member}, Guild ${event.guild} has tellOnJoin = ${joinLeaveConfig.tellOnJoin} and the joinMessage is ${joinLeaveConfig.joinMessage}, canalJoinId = ${joinLeaveConfig.canalJoinId}" }

		if (joinLeaveConfig.tellOnJoin && joinLeaveConfig.joinMessage.isNotEmpty()) { // E o sistema de avisar ao entrar está ativado?
			logger.trace { "Guild ${event.guild} has tellOnJoin enabled and the joinMessage isn't empty!" }
			val guild = event.guild

			logger.debug { "Member = ${event.member}, Getting ${guild}'s cache list from joinMembersCache..."}

			val list = joinMembersCache.getIfPresent(event.guild.idLong) ?: CopyOnWriteArrayList()
			logger.debug { "Member = ${event.member}, There are ${list.size} entries on the joinMembersCache list for $guild"}
			list.add(event.user)
			joinMembersCache.put(event.guild.idLong, list)

			logger.trace { "Member = ${event.member}, Checking if the joinMembersCache max list entry threshold is > 20 for $guild, currently it is ${list.size}"}

			if (list.size > 20)
				return

			if (joinLeaveConfig.canalJoinId != null) {
				logger.trace { "Member = ${event.member}, canalJoinId is not null for $guild, canalJoinId = ${joinLeaveConfig.canalJoinId}"}

				val textChannel = guild.getTextChannelByNullableId(joinLeaveConfig.canalJoinId)

				logger.trace { "Member = ${event.member}, canalLeaveId = ${joinLeaveConfig.canalLeaveId}, it is $textChannel for $guild"}
				if (textChannel != null) {
					logger.trace { "Member = ${event.member}, Text channel $textChannel is not null for $guild! Can I talk? ${textChannel.canTalk()}" }

					if (textChannel.canTalk()) {
						val msg = joinLeaveConfig.joinMessage
						logger.trace { "Member = ${event.member}, Join message is $msg for $guild, it will be sent at $textChannel"}

						if (msg.isNotEmpty() && event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS)) {
							logger.debug { "Member = ${event.member}, Sending quit message \"$msg\" in $textChannel at $guild"}

							textChannel.sendMessage(MessageUtils.generateMessage(msg, listOf(guild, event.member), guild, tokens)!!).queue {
								if (legacyServerConfig.joinLeaveConfig.deleteJoinMessagesAfter != null)
									it.delete().queueAfter(legacyServerConfig.joinLeaveConfig.deleteJoinMessagesAfter!!, TimeUnit.SECONDS)
							}
						}
					} else {
						logger.debug { "Member = ${event.member} (Join), I don't have permission to send messages in $textChannel on guild $guild!" }
						LorittaUtils.warnOwnerNoPermission(guild, textChannel, serverConfig)
					}
				}
			}
		}

		logger.trace { "Member = ${event.member}, Guild ${event.guild} has tellOnPrivate = ${joinLeaveConfig.tellOnPrivate} and the joinMessage is ${joinLeaveConfig.joinPrivateMessage}" }

		if (!event.user.isBot && joinLeaveConfig.tellOnPrivate && joinLeaveConfig.joinPrivateMessage.isNotEmpty()) { // Talvez o sistema de avisar no privado esteja ativado!
			val msg = joinLeaveConfig.joinPrivateMessage

			logger.debug { "Member = ${event.member}, sending join message (private channel) \"$msg\" at ${event.guild}"}

			if (msg.isNotEmpty()) {
				event.user.openPrivateChannel().queue {
					it.sendMessage(MessageUtils.generateMessage(msg, listOf(event.guild, event.member), event.guild, tokens)!!).queue() // Pronto!
				}
			}
		}
	}

	suspend fun handleLeave(event: GuildMemberLeaveEvent, serverConfig: ServerConfig, legacyServerConfig: MongoServerConfig) {
		val joinLeaveConfig = legacyServerConfig.joinLeaveConfig

		logger.trace { "Member = ${event.member}, Guild ${event.guild} has tellOnLeave = ${joinLeaveConfig.tellOnLeave} and the leaveMessage is ${joinLeaveConfig.leaveMessage}, canalLeaveId = ${joinLeaveConfig.canalLeaveId}" }
		if (joinLeaveConfig.tellOnLeave && joinLeaveConfig.leaveMessage.isNotEmpty()) {
			logger.trace { "Member = ${event.member}, Guild ${event.guild} has tellOnLeave enabled and the leaveMessage isn't empty!" }
			val guild = event.guild

			logger.debug { "Member = ${event.member}, Getting ${guild}'s cache list from leftMembersCache..."}

			val list = leftMembersCache.getIfPresent(event.guild.idLong) ?: CopyOnWriteArrayList()
			logger.debug { "Member = ${event.member}, There are ${list.size} entries on the leftMembersCache list for $guild"}
			list.add(event.user)
			leftMembersCache.put(event.guild.idLong, list)

			logger.trace { "Member = ${event.member}, Checking if the leftMembersCache max list entry threshold is > 20 for $guild, currently it is ${list.size}"}

			if (list.size > 20)
				return

			if (joinLeaveConfig.canalLeaveId != null) {
				logger.trace { "Member = ${event.member}, canalLeaveId is not null for $guild, canalLeaveId = ${joinLeaveConfig.canalLeaveId}"}

				val textChannel = guild.getTextChannelByNullableId(joinLeaveConfig.canalLeaveId)

				logger.trace { "Member = ${event.member}, canalLeaveId = ${joinLeaveConfig.canalLeaveId}, it is $textChannel for $guild"}
				if (textChannel != null) {
					logger.trace { "Member = ${event.member}, Text channel $textChannel is not null for $guild! Can I talk? ${textChannel.canTalk()}" }

					if (textChannel.canTalk()) {
						var msg = joinLeaveConfig.leaveMessage
						logger.trace { "Member = ${event.member}, Leave message is $msg for $guild, it will be sent at $textChannel"}

						val customTokens = mutableMapOf<String, String>()

						// Verificar se o usuário foi banido e, se sim, mudar a mensagem caso necessário
						val bannedUserKey = "${event.guild.id}#${event.user.id}"

						if (joinLeaveConfig.tellOnBan && EventLogListener.bannedUsers.getIfPresent(bannedUserKey) == true) {
							if (joinLeaveConfig.banMessage.isNotEmpty())
								msg = joinLeaveConfig.banMessage
						}
						// Invalidar, já que a Loritta faz cache mesmo que o servidor não use a função
						EventLogListener.bannedUsers.invalidate(bannedUserKey)

						if (msg.isNotEmpty()) {
							logger.debug { "Member = ${event.member}, Sending quit message \"$msg\" in $textChannel at $guild"}

							textChannel.sendMessage(MessageUtils.generateMessage(msg, listOf(event.guild, event.member), guild, customTokens)!!).queue {
								if (legacyServerConfig.joinLeaveConfig.deleteLeaveMessagesAfter != null)
									it.delete().queueAfter(legacyServerConfig.joinLeaveConfig.deleteLeaveMessagesAfter!!, TimeUnit.SECONDS)
							}
						}
					} else {
						logger.debug { "Member = ${event.member} (Quit), I don't have permission to send messages in $textChannel on guild $guild!" }
						LorittaUtils.warnOwnerNoPermission(guild, textChannel, serverConfig)
					}
				}
			}
		}
	}
}