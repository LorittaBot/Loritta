package net.perfectdreams.loritta.morenitta.utils.eventlog

import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.unions.AudioChannelUnion
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.Role
import net.dv8tion.jda.api.entities.channel.Channel
import net.dv8tion.jda.api.utils.FileUpload
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.dao.StoredMessage
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.EventLogConfig
import net.perfectdreams.loritta.morenitta.messageverify.LoriMessageDataUtils
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import java.awt.Color
import java.time.Instant
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object EventLog {
	private val logger by HarmonyLoggerFactory.logger {}

	suspend fun onMessageReceived(loritta: LorittaBot, serverConfig: ServerConfig, message: Message) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && (eventLogConfig.messageDeleted || eventLogConfig.messageEdited)) {
				val savedMessage = LoriMessageDataUtils.convertMessageToSavedMessage(message)

				loritta.newSuspendedTransaction {
					StoredMessage.new(message.idLong) {
						this.authorId = message.author.idLong
						this.channelId = message.channel.idLong
						this.createdAt = Instant.now()
						this.savedMessageDataVersion = 1
						this.encryptAndSetContent(loritta, savedMessage)
					}
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao salvar mensagem do event log" }
		}
	}

	suspend fun onMessageUpdate(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, message: Message) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && (eventLogConfig.messageEdited || eventLogConfig.messageDeleted)) {
				val storedMessage = loritta.newSuspendedTransaction {
					StoredMessage.findById(message.idLong)
				}

				if (storedMessage != null) {
					val savedMessage = storedMessage.decryptContent(loritta)

					loritta.newSuspendedTransaction {
						storedMessage.encryptAndSetContent(loritta, LoriMessageDataUtils.convertMessageToSavedMessage(message))
					}

					val textChannel = message.guild.getGuildMessageChannelById(eventLogConfig.messageEditedLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

					if (textChannel.canTalk() && message.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && message.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL) && message.guild.selfMember.hasPermission(Permission.MESSAGE_ATTACH_FILES)) {
						if (savedMessage.content != message.contentRaw && eventLogConfig.messageEdited) {
							val embed = EmbedBuilder()
								.setColor(Color(238, 241, 0).rgb)
								.setDescription(
									"\uD83D\uDCDD ${
										locale.getList(
											"modules.eventLog.messageEdited",
											message.member?.asMention,
											savedMessage.content,
											message.contentRaw,
											message.guildChannel.asMention
										).joinToString("\n")
									}"
								)
								.setAuthor(
									"${message.member?.user?.name}#${message.member?.user?.discriminator}",
									null,
									message.member?.user?.effectiveAvatarUrl
								)
								.setFooter(locale["modules.eventLog.userID", message.member?.user?.id], null)
								.setTimestamp(Instant.now())

							val fileName = LoriMessageDataUtils.createFileNameForSavedMessageImage(savedMessage)
							embed.setImage("attachment://$fileName")

							val finalImage =
								LoriMessageDataUtils.createSignedRenderedSavedMessage(loritta, savedMessage, true)

							textChannel.sendMessageEmbeds(embed.build())
								.addFiles(FileUpload.fromData(finalImage, fileName))
								.await()
						}
					}
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao atualizar mensagem do event log" }
		}
	}

	suspend fun onMessageDelete(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, messageId: Long, channelId: Long, guildId: Long) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.messageDeleted) {
				val storedMessage = loritta.newSuspendedTransaction {
					StoredMessage.findById(messageId)
				}

				if (storedMessage != null) {
					val savedMessage = storedMessage.decryptContent(loritta)
					val guild = loritta.lorittaShards.getGuildById(guildId) ?: return
					val textChannel = guild.getGuildMessageChannelById(eventLogConfig.messageDeletedLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

					if (textChannel.canTalk() && guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && guild.selfMember.hasPermission(Permission.VIEW_CHANNEL) && guild.selfMember.hasPermission(Permission.MESSAGE_ATTACH_FILES)) {
						val author = guild.getMemberById(storedMessage.authorId)
						val channel = guild.getGuildChannelById(channelId)

						val embed = EmbedBuilder()
							.setColor(Color(221, 0, 0).rgb)
							.setDescription(
								"${locale["modules.eventLog.messageDeleted", author?.asMention ?: "Unknown User", savedMessage.content, channel?.asMention ?: "Unknown Channel"]}"
							)
							.setAuthor(
								"${author?.user?.name ?: "Unknown"}#${author?.user?.discriminator ?: "0000"}",
								null,
								author?.user?.effectiveAvatarUrl
							)
							.setFooter(locale["modules.eventLog.userID", storedMessage.authorId], null)
							.setTimestamp(Instant.now())

						val fileName = LoriMessageDataUtils.createFileNameForSavedMessageImage(savedMessage)
						embed.setImage("attachment://$fileName")

						val finalImage = LoriMessageDataUtils.createSignedRenderedSavedMessage(loritta, savedMessage, true)

						textChannel.sendMessageEmbeds(embed.build())
							.addFiles(FileUpload.fromData(finalImage, fileName))
							.await()
					}

					loritta.newSuspendedTransaction {
						storedMessage.delete()
					}
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao deletar mensagem do event log" }
		}
	}

	suspend fun onImageDelete(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, messageId: Long, channelId: Long, guildId: Long) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.imageDeleted) {
				val storedMessage = loritta.newSuspendedTransaction {
					StoredMessage.findById(messageId)
				}

				if (storedMessage != null) {
					val savedMessage = storedMessage.decryptContent(loritta)
					
					if (savedMessage.attachments.isEmpty()) return

					val guild = loritta.lorittaShards.getGuildById(guildId) ?: return
					val textChannel = guild.getGuildMessageChannelById(eventLogConfig.imageDeletedLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

					if (textChannel.canTalk() && guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
						val author = guild.getMemberById(storedMessage.authorId)
						val channel = guild.getGuildChannelById(channelId)

						val embed = EmbedBuilder()
							.setColor(Color(221, 0, 0).rgb)
							.setDescription(
								locale["modules.eventLog.imageDeleted", author?.asMention ?: "Unknown User", channel?.asMention ?: "Unknown Channel"]
							)
							.setAuthor(
								"${author?.user?.name ?: "Unknown"}#${author?.user?.discriminator ?: "0000"}",
								null,
								author?.user?.effectiveAvatarUrl
							)
							.setFooter(locale["modules.eventLog.userID", storedMessage.authorId], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao deletar imagem do event log" }
		}
	}

	suspend fun onBulkMessageDelete(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, messageIds: List<Long>, channelId: Long, guildId: Long) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.bulkMessageDeleted) {
				val guild = loritta.lorittaShards.getGuildById(guildId) ?: return
				val textChannel = guild.getGuildMessageChannelById(eventLogConfig.bulkMessageDeletedLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val channel = guild.getGuildChannelById(channelId)

					val embed = EmbedBuilder()
						.setColor(Color(221, 0, 0).rgb)
						.setDescription(
							locale["modules.eventLog.bulkMessageDeleted", messageIds.size.toString(), channel?.asMention ?: "Unknown Channel"]
						)
						.setFooter(locale["modules.eventLog.messageCount", messageIds.size], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}

				loritta.newSuspendedTransaction {
					messageIds.forEach { messageId ->
						StoredMessage.findById(messageId)?.delete()
					}
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao deletar mensagens em massa do event log" }
		}
	}

	suspend fun onInviteCreate(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, inviteCode: String, inviter: User?, channelId: Long, guildId: Long, maxUses: Int, maxAge: Int) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.inviteCreated) {
				val guild = loritta.lorittaShards.getGuildById(guildId) ?: return
				val textChannel = guild.getGuildMessageChannelById(eventLogConfig.inviteCreatedLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val channel = guild.getGuildChannelById(channelId)

					val embed = EmbedBuilder()
						.setColor(Color(87, 242, 135).rgb)
						.setDescription(
							locale["modules.eventLog.inviteCreated", inviter?.asMention ?: "Unknown User", inviteCode, channel?.asMention ?: "Unknown Channel", if (maxUses == 0) "∞" else maxUses.toString(), if (maxAge == 0) "∞" else "${maxAge / 3600}h"]
						)
						.setAuthor(
							"${inviter?.name ?: "Unknown"}#${inviter?.discriminator ?: "0000"}",
							null,
							inviter?.effectiveAvatarUrl
						)
						.setFooter(locale["modules.eventLog.userID", inviter?.id ?: "Unknown"], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao criar convite do event log" }
		}
	}

	suspend fun onModeratorCommand(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, moderator: Member, command: String, target: Member?, reason: String?) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.moderatorCommands) {
				val textChannel = moderator.guild.getGuildMessageChannelById(eventLogConfig.moderatorCommandsLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && moderator.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && moderator.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val embed = EmbedBuilder()
						.setColor(Color(255, 165, 0).rgb)
						.setDescription(
							locale["modules.eventLog.moderatorCommand", moderator.asMention, command, target?.asMention ?: "N/A", reason ?: "No reason provided"]
						)
						.setAuthor(
							"${moderator.user.name}#${moderator.user.discriminator}",
							null,
							moderator.user.effectiveAvatarUrl
						)
						.setFooter(locale["modules.eventLog.userID", moderator.user.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao registrar comando de moderador do event log" }
		}
	}

	suspend fun onMemberJoin(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, member: Member) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.memberJoin) {
				val textChannel = member.guild.getGuildMessageChannelById(eventLogConfig.memberJoinLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val embed = EmbedBuilder()
						.setColor(Color(87, 242, 135).rgb)
						.setDescription(locale["modules.eventLog.memberJoin", member.asMention])
						.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
						.setFooter(locale["modules.eventLog.userID", member.user.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao entrar membro do event log" }
		}
	}

	suspend fun onMemberLeave(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, member: Member) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.memberLeave) {
				val textChannel = member.guild.getGuildMessageChannelById(eventLogConfig.memberLeaveLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val embed = EmbedBuilder()
						.setColor(Color(221, 0, 0).rgb)
						.setDescription(locale["modules.eventLog.memberLeave", member.asMention])
						.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
						.setFooter(locale["modules.eventLog.userID", member.user.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao sair membro do event log" }
		}
	}

	suspend fun onMemberRoleAdd(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, member: Member, role: Role) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.memberRoleAdd) {
				val textChannel = member.guild.getGuildMessageChannelById(eventLogConfig.memberRoleAddLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val embed = EmbedBuilder()
						.setColor(Color(87, 242, 135).rgb)
						.setDescription(locale["modules.eventLog.memberRoleAdd", member.asMention, role.asMention])
						.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
						.setFooter(locale["modules.eventLog.userID", member.user.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao adicionar cargo do event log" }
		}
	}

	suspend fun onMemberRoleRemove(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, member: Member, role: Role) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.memberRoleRemove) {
				val textChannel = member.guild.getGuildMessageChannelById(eventLogConfig.memberRoleRemoveLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val embed = EmbedBuilder()
						.setColor(Color(221, 0, 0).rgb)
						.setDescription(locale["modules.eventLog.memberRoleRemove", member.asMention, role.asMention])
						.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
						.setFooter(locale["modules.eventLog.userID", member.user.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao remover cargo do event log" }
		}
	}

	suspend fun onMemberTimeout(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, member: Member, timeoutEnd: Instant?) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.memberTimeout) {
				val textChannel = member.guild.getGuildMessageChannelById(eventLogConfig.memberTimeoutLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val embed = EmbedBuilder()
						.setColor(Color(255, 165, 0).rgb)
						.setDescription(
							if (timeoutEnd != null) {
								locale["modules.eventLog.memberTimeout", member.asMention, timeoutEnd.toString()]
							} else {
								locale["modules.eventLog.memberTimeoutRemoved", member.asMention]
							}
						)
						.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
						.setFooter(locale["modules.eventLog.userID", member.user.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao aplicar timeout do event log" }
		}
	}

    suspend fun onNicknameChange(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, member: Member, oldNickname: String?, newNickname: String?) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.nicknameChange) {
				val textChannel = member.guild.getGuildMessageChannelById(eventLogConfig.nicknameChangeLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val embed = EmbedBuilder()
						.setColor(Color(238, 241, 0).rgb)
						.setDescription(locale["modules.eventLog.nicknameChange", member.asMention, oldNickname ?: "None", newNickname ?: "None"])
						.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
						.setFooter(locale["modules.eventLog.userID", member.user.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao mudar nickname do event log" }
		}
	}

	suspend fun onMemberBan(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, user: User, guildId: Long, reason: String?) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.memberBan) {
				val guild = loritta.lorittaShards.getGuildById(guildId) ?: return
				val textChannel = guild.getGuildMessageChannelById(eventLogConfig.memberBanLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val embed = EmbedBuilder()
						.setColor(Color(221, 0, 0).rgb)
						.setDescription(locale["modules.eventLog.memberBan", user.asMention, reason ?: "No reason provided"])
						.setAuthor("${user.name}#${user.discriminator}", null, user.effectiveAvatarUrl)
						.setFooter(locale["modules.eventLog.userID", user.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao banir membro do event log" }
		}
	}

	suspend fun onMemberUnban(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, user: User, guildId: Long) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.memberUnban) {
				val guild = loritta.lorittaShards.getGuildById(guildId) ?: return
				val textChannel = guild.getGuildMessageChannelById(eventLogConfig.memberUnbanLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val embed = EmbedBuilder()
						.setColor(Color(87, 242, 135).rgb)
						.setDescription(locale["modules.eventLog.memberUnban", user.asMention])
						.setAuthor("${user.name}#${user.discriminator}", null, user.effectiveAvatarUrl)
						.setFooter(locale["modules.eventLog.userID", user.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao desbanir membro do event log" }
		}
	}

	suspend fun onRoleCreate(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, role: Role) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.roleCreate) {
				val textChannel = role.guild.getGuildMessageChannelById(eventLogConfig.roleCreateLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && role.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && role.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val embed = EmbedBuilder()
						.setColor(Color(87, 242, 135).rgb)
						.setDescription(locale["modules.eventLog.roleCreate", role.asMention])
						.setFooter(locale["modules.eventLog.roleID", role.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao criar cargo do event log" }
		}
	}

	suspend fun onRoleDelete(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, roleName: String, roleId: Long, guildId: Long) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.roleDelete) {
				val guild = loritta.lorittaShards.getGuildById(guildId) ?: return
				val textChannel = guild.getGuildMessageChannelById(eventLogConfig.roleDeleteLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val embed = EmbedBuilder()
						.setColor(Color(221, 0, 0).rgb)
						.setDescription(locale["modules.eventLog.roleDelete", roleName])
						.setFooter(locale["modules.eventLog.roleID", roleId], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao deletar cargo do event log" }
		}
	}

	suspend fun onRoleUpdate(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, oldRole: Role, newRole: Role) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.roleUpdate) {
				val textChannel = newRole.guild.getGuildMessageChannelById(eventLogConfig.roleUpdateLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && newRole.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && newRole.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val changes = mutableListOf<String>()
					
					if (oldRole.name != newRole.name) {
						changes.add("Name: ${oldRole.name} → ${newRole.name}")
					}
					if (oldRole.color != newRole.color) {
						changes.add("Color: ${oldRole.color} → ${newRole.color}")
					}
					if (oldRole.permissions != newRole.permissions) {
						changes.add("Permissions updated")
					}
					if (oldRole.isHoisted != newRole.isHoisted) {
						changes.add("Hoisted: ${oldRole.isHoisted} → ${newRole.isHoisted}")
					}

					if (changes.isNotEmpty()) {
						val embed = EmbedBuilder()
							.setColor(Color(238, 241, 0).rgb)
							.setDescription(locale["modules.eventLog.roleUpdate", newRole.asMention] + "\n\n${changes.joinToString("\n")}")
							.setFooter(locale["modules.eventLog.roleID", newRole.id], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao atualizar cargo do event log" }
		}
	}

	suspend fun onChannelCreate(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, channel: Channel) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.channelCreate) {
				val guild = channel.guild ?: return
				val textChannel = guild.getGuildMessageChannelById(eventLogConfig.channelCreateLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val embed = EmbedBuilder()
						.setColor(Color(87, 242, 135).rgb)
						.setDescription(locale["modules.eventLog.channelCreate", channel.name, channel.type.toString()])
						.setFooter(locale["modules.eventLog.channelID", channel.id], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao criar canal do event log" }
		}
	}

	suspend fun onChannelUpdate(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, oldChannel: Channel, newChannel: Channel) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.channelUpdate) {
				val guild = newChannel.guild ?: return
				val textChannel = guild.getGuildMessageChannelById(eventLogConfig.channelUpdateLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val changes = mutableListOf<String>()
					
					if (oldChannel.name != newChannel.name) {
						changes.add("Name: ${oldChannel.name} → ${newChannel.name}")
					}

					if (changes.isNotEmpty()) {
						val embed = EmbedBuilder()
							.setColor(Color(238, 241, 0).rgb)
							.setDescription(locale["modules.eventLog.channelUpdate", newChannel.name] + "\n\n${changes.joinToString("\n")}")
							.setFooter(locale["modules.eventLog.channelID", newChannel.id], null)
							.setTimestamp(Instant.now())

						textChannel.sendMessageEmbeds(embed.build()).await()
					}
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao atualizar canal do event log" }
		}
	}

	suspend fun onChannelDelete(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, channelName: String, channelType: String, channelId: Long, guildId: Long) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.channelDelete) {
				val guild = loritta.lorittaShards.getGuildById(guildId) ?: return
				val textChannel = guild.getGuildMessageChannelById(eventLogConfig.channelDeleteLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (textChannel.canTalk() && guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS) && guild.selfMember.hasPermission(Permission.VIEW_CHANNEL)) {
					val embed = EmbedBuilder()
						.setColor(Color(221, 0, 0).rgb)
						.setDescription(locale["modules.eventLog.channelDelete", channelName, channelType])
						.setFooter(locale["modules.eventLog.channelID", channelId], null)
						.setTimestamp(Instant.now())

					textChannel.sendMessageEmbeds(embed.build()).await()
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao deletar canal do event log" }
		}
	}

	suspend fun onVoiceJoin(loritta: LorittaBot, serverConfig: ServerConfig, member: Member, channelJoined: AudioChannelUnion) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.voiceChannelJoins) {
				val textChannel = member.guild.getGuildMessageChannelById(eventLogConfig.voiceChannelJoinsLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)

				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return

				val embed = EmbedBuilder()
					.setColor(Color(35, 209, 96).rgb)
					.setDescription("\uD83D\uDC49\uD83C\uDFA4 **${locale["modules.eventLog.joinedVoiceChannel", member.asMention, channelJoined.name]}**")
					.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
					.setFooter(locale["modules.eventLog.userID", member.user.id], null)
					.setTimestamp(Instant.now())
					.build()

				textChannel.sendMessageEmbeds(embed).await()
				return
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao entrar no canal de voz do event log" }
		}
	}

	suspend fun onVoiceLeave(loritta: LorittaBot, serverConfig: ServerConfig, member: Member, channelLeft: AudioChannelUnion) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.voiceChannelLeaves) {
				val textChannel = member.guild.getGuildMessageChannelById(eventLogConfig.voiceChannelLeavesLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return
				val locale = loritta.localeManager.getLocaleById(serverConfig.localeId)
				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return

				val embed = EmbedBuilder()
					.setColor(Color(35, 209, 96).rgb)
					.setDescription("\uD83D\uDC48\uD83C\uDFA4 **${locale["modules.eventLog.leftVoiceChannel", member.asMention, channelLeft.name]}**")
					.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
					.setFooter(locale["modules.eventLog.userID", member.user.id], null)
					.setTimestamp(Instant.now())
					.build()

				textChannel.sendMessageEmbeds(embed).await()
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao sair do canal de voz do event log" }
		}
	}

	suspend fun onVoiceMove(loritta: LorittaBot, serverConfig: ServerConfig, locale: BaseLocale, member: Member, channelLeft: AudioChannelUnion, channelJoined: AudioChannelUnion) {
		try {
			val eventLogConfig = serverConfig.getCachedOrRetreiveFromDatabaseAsync<EventLogConfig?>(loritta, ServerConfig::eventLogConfig) ?: return

			if (eventLogConfig.enabled && eventLogConfig.voiceChannelMove) {
				val textChannel = member.guild.getGuildMessageChannelById(eventLogConfig.voiceChannelMoveLogChannelId ?: eventLogConfig.eventLogChannelId) ?: return

				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return

				val embed = EmbedBuilder()
					.setColor(Color(238, 241, 0).rgb)
					.setDescription(locale["modules.eventLog.movedVoiceChannel", member.asMention, channelLeft.name, channelJoined.name])
					.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
					.setFooter(locale["modules.eventLog.userID", member.user.id], null)
					.setTimestamp(Instant.now())
					.build()

				textChannel.sendMessageEmbeds(embed).await()
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao mover canal de voz do event log" }
		}
	}

	private fun generateRandomInitVector(loritta: LorittaBot) = ByteArray(16).apply {
		loritta.random.nextBytes(this)
	}

	fun encryptMessage(loritta: LorittaBot, content: String): EncryptedMessage {
		val initVector = generateRandomInitVector(loritta)

		val iv = IvParameterSpec(initVector)
		val skeySpec = SecretKeySpec(loritta.config.loritta.messageEncryption.encryptionKey.toByteArray(charset("UTF-8")), "AES")

		val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
		cipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
		val encrypted = cipher.doFinal(content.toByteArray())
		return EncryptedMessage(Base64.getEncoder().encodeToString(initVector), Base64.getEncoder().encodeToString(encrypted))
	}

	fun decryptMessage(loritta: LorittaBot, initVector: String, encryptedContent: String): String {
		val iv = IvParameterSpec(Base64.getDecoder().decode(initVector))
		val skeySpec = SecretKeySpec(loritta.config.loritta.messageEncryption.encryptionKey.toByteArray(charset("UTF-8")), "AES")

		val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
		cipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)
		val original = cipher.doFinal(Base64.getDecoder().decode(encryptedContent))

		return String(original)
	}

	data class EncryptedMessage(
		val initializationVector: String,
		val encryptedMessage: String
	)
}
