package com.mrpowergamerbr.loritta.listeners

import com.github.benmanes.caffeine.cache.Caffeine
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.debug.DebugLog
import com.mrpowergamerbr.loritta.utils.eventlog.StoredMessage
import com.mrpowergamerbr.loritta.utils.misc.PomfUtils
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.audit.ActionType
import net.dv8tion.jda.core.events.channel.text.GenericTextChannelEvent
import net.dv8tion.jda.core.events.channel.text.TextChannelCreateEvent
import net.dv8tion.jda.core.events.channel.text.TextChannelDeleteEvent
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdateNameEvent
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdatePositionEvent
import net.dv8tion.jda.core.events.channel.text.update.TextChannelUpdateTopicEvent
import net.dv8tion.jda.core.events.guild.GuildBanEvent
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.core.events.user.update.UserUpdateAvatarEvent
import net.dv8tion.jda.core.events.user.update.UserUpdateNameEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import org.bson.Document
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.net.URL
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO

class EventLogListener(internal val loritta: Loritta) : ListenerAdapter() {
	val handledUsernameChanges = Caffeine.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).maximumSize(100).build<Any, Any>().asMap()
	val logger by logger()

	override fun onUserUpdateAvatar(event: UserUpdateAvatarEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			val embed = EmbedBuilder()
			embed.setTimestamp(Instant.now())
			embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
			embed.setColor(Constants.DISCORD_BLURPLE)
			embed.setImage("attachment://avatar.png")

			val rawOldAvatar = LorittaUtils.downloadImage(if (event.oldAvatarUrl == null) event.user.defaultAvatarUrl else event.oldAvatarUrl.replace("jpg", "png"))
			val rawNewAvatar = LorittaUtils.downloadImage(event.user.effectiveAvatarUrl.replace("jpg", "png"))

			if (rawOldAvatar == null || rawNewAvatar == null) // As vezes o avatar pode ser null
				return@execute

			val oldAvatar = rawOldAvatar.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH)
			val newAvatar = rawNewAvatar.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH)

			val base = BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB_PRE)
			val graphics = base.graphics
			graphics.drawImage(oldAvatar, 0, 0, null)
			graphics.drawImage(newAvatar, 128, 0, null)

			ByteArrayOutputStream().use { baos ->
				ImageIO.write(base, "png", baos)

				ByteArrayInputStream(baos.toByteArray()).use { bais ->
					// E agora nós iremos anunciar a troca para todos os servidores
					val guilds = event.jda.guilds.filter { it.isMember(event.user) }

					loritta.serversColl.find(
							Filters.and(
									Filters.eq("eventLogConfig.avatarChanges", true),
									Filters.eq("eventLogConfig.enabled", true),
									Filters.`in`("_id", guilds.map { it.id })
							)
					).iterator().use {
						while (it.hasNext()) {
							val config = it.next()
							val locale = loritta.getLocaleById(config.localeId)

							val guild = guilds.first { it.id == config.guildId }

							val textChannel = guild.getTextChannelById(config.eventLogConfig.eventLogChannelId)

							if (textChannel != null && textChannel.canTalk()) {
								if (!guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
									continue
								if (!guild.selfMember.hasPermission(Permission.MESSAGE_ATTACH_FILES))
									continue
								if (!guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
									continue
								if (!guild.selfMember.hasPermission(Permission.MESSAGE_READ))
									continue

								embed.setDescription("\uD83D\uDDBC ${locale.get("EVENTLOG_AVATAR_CHANGED", event.user.asMention)}")
								embed.setFooter(locale["EVENTLOG_USER_ID", event.user.id], null)

								val message = MessageBuilder().append(" ").setEmbed(embed.build())

								textChannel.sendFile(bais, "avatar.png", message.build()).complete()
							}
						}
					}
				}
			}
		}
	}

	override fun onUserUpdateName(event: UserUpdateNameEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			val embed = EmbedBuilder()
			embed.setTimestamp(Instant.now())
			embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
			embed.setColor(Constants.DISCORD_BLURPLE)
			embed.setImage("attachment://avatar.png")
			if (!handledUsernameChanges.containsKey(event.user.id)) {
				// É necessário fazer isto já que todas as shards irão receber a notificação de username change
				handledUsernameChanges.put(event.user.id, System.currentTimeMillis())
				val newName = event.user.name
				val newDiscriminator = event.user.discriminator
				val changedAt = System.currentTimeMillis()

				val changeWrapper = LorittaProfile.UsernameChange(changedAt, newName, newDiscriminator)

				val profile = loritta.getLorittaProfileForUser(event.user.id)

				if (profile.usernameChanges.isEmpty()) {
					profile.usernameChanges.add((LorittaProfile.UsernameChange(event.user.creationTime.toEpochSecond() * 1000, event.user.name, event.user.discriminator)))
				}

				profile.usernameChanges.add(changeWrapper)

				loritta save profile
			}

			val guilds = event.jda.guilds.filter { it.isMember(event.user) }

			loritta.serversColl.find(
					Filters.and(
							Filters.eq("eventLogConfig.usernameChanges", true),
							Filters.eq("eventLogConfig.enabled", true),
							Filters.`in`("_id", guilds.map { it.id })
					)
			).iterator().use {
				while (it.hasNext()) {
					val config = it.next()
					val locale = loritta.getLocaleById(config.localeId)

					val guild = guilds.first { it.id == config.guildId }

					val textChannel = guild.getTextChannelById(config.eventLogConfig.eventLogChannelId)

					if (textChannel != null && textChannel.canTalk()) {
						if (!guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
							continue
						if (!guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
							continue
						if (!guild.selfMember.hasPermission(Permission.MESSAGE_READ))
							continue

						embed.setDescription("\uD83D\uDCDD ${locale["EVENTLOG_NAME_CHANGED", event.user.asMention, "${event.oldName}#${event.oldValue}", "${event.user.name}#${event.user.discriminator}"]}")
						embed.setFooter(locale["EVENTLOG_USER_ID", event.user.id], null)

						textChannel.sendMessage(embed.build()).complete()
					}
				}
			}
		}
	}

	// TEXT CHANNEL
	override fun onGenericTextChannel(event: GenericTextChannelEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			val embed = EmbedBuilder()
			embed.setTimestamp(Instant.now())
			embed.setColor(Color(35, 209, 96))
			embed.setAuthor(event.guild.name, null, event.guild.iconUrl)

			val config = loritta.getServerConfigForGuild(event.guild.id)
			val locale = loritta.getLocaleById(config.localeId)
			val eventLogConfig = config.eventLogConfig
			if (eventLogConfig.isEnabled) {
				val textChannel = event.guild.getTextChannelById(config.eventLogConfig.eventLogChannelId);

				if (textChannel != null && textChannel.canTalk()) {
					if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
						return@execute
					if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
						return@execute
					if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
						return@execute

					if (event is TextChannelCreateEvent && eventLogConfig.channelCreated) {
						embed.setDescription("\uD83C\uDF1F ${locale["EVENTLOG_CHANNEL_CREATED", event.channel.asMention]}")

						textChannel.sendMessage(embed.build()).complete()
						return@execute
					}
					if (event is TextChannelUpdateNameEvent && eventLogConfig.channelNameUpdated) {
						embed.setDescription("\uD83D\uDCDD ${locale["EVENTLOG_CHANNEL_NAME_UPDATED", event.channel.asMention, event.oldName, event.channel.name]}")

						textChannel.sendMessage(embed.build()).complete()
						return@execute
					}
					if (event is TextChannelUpdateTopicEvent && eventLogConfig.channelTopicUpdated) {
						embed.setDescription("\uD83D\uDCDD ${locale["EVENTLOG_CHANNEL_TOPIC_UPDATED", event.channel.asMention, event.oldTopic, event.channel.topic]}")

						textChannel.sendMessage(embed.build()).complete()
						return@execute
					}
					if (event is TextChannelUpdatePositionEvent && eventLogConfig.channelPositionUpdated) {
						embed.setDescription("\uD83D\uDCDD ${locale["EVENTLOG_CHANNEL_POSITION_UPDATED", event.channel.asMention, event.oldPosition, event.channel.position]}")

						textChannel.sendMessage(embed.build()).complete()
						return@execute
					}
					if (event is TextChannelDeleteEvent && eventLogConfig.channelDeleted) {
						embed.setDescription("\uD83D\uDEAE ${locale["EVENTLOG_CHANNEL_DELETED", event.channel.name]}")

						textChannel.sendMessage(embed.build()).complete()
						return@execute
					}
				}
			}
		}
	}

	// Mensagens
	override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			val eventLogConfig = loritta.getServerConfigForGuild(event.guild.id).eventLogConfig

			if (eventLogConfig.isEnabled && (eventLogConfig.messageDeleted || eventLogConfig.messageEdit)) {
				val attachments = mutableListOf<String>()

				event.message.attachments.forEach {
					attachments.add(it.url)
				}

				loritta save StoredMessage(event.message.id, event.author.name + "#" + event.author.discriminator, event.message.contentRaw, event.author.id, event.message.channel.id, attachments)

				// Agora nós iremos fazer reupload dos attachments para o pomf
				val reuploadedAttachments = mutableListOf<String>()

				for (attachmentUrl in attachments) {
					val url = URL(attachmentUrl)
					val conn = url.openConnection()
					conn.setRequestProperty("User-Agent", Constants.USER_AGENT)
					val content = conn.getInputStream().use { it.readBytes() }
					val split = attachmentUrl.split("/")
					val pomfUrl = PomfUtils.uploadFile(content, split.last())

					reuploadedAttachments.add(pomfUrl ?: attachmentUrl)
				}

				if (reuploadedAttachments.isNotEmpty()) {
					// E depois iremos atualizar caso ainda exista uma mensagem com o ID desejado
					loritta.storedMessagesColl.updateOne(
							Filters.eq("_id", event.message.id),
							Document("\$set", Document("attachments", reuploadedAttachments))
					)
				}
			}
		}
	}

	override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			val config = loritta.getServerConfigForGuild(event.guild.id)
			val locale = loritta.getLocaleById(config.localeId)
			val eventLogConfig = config.eventLogConfig

			if (eventLogConfig.isEnabled && (eventLogConfig.messageEdit || eventLogConfig.messageDeleted)) {
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId)
				if (textChannel != null && textChannel.canTalk()) {
					if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
						return@execute
					if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
						return@execute
					if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
						return@execute

					val storedMessage = loritta.storedMessagesColl.find(Filters.eq("_id", event.message.id)).first()
					if (storedMessage != null && storedMessage.content != event.message.contentRaw) {
						val embed = EmbedBuilder()
						embed.setTimestamp(Instant.now())

						embed.setColor(Color(238, 241, 0))

						embed.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)
						embed.setDescription("\uD83D\uDCDD ${locale.get("EVENTLOG_MESSAGE_EDITED", event.member.asMention, storedMessage.content, event.message.contentRaw, event.message.textChannel.asMention)}")
						embed.setFooter(locale["EVENTLOG_USER_ID", event.member.user.id], null)

						textChannel.sendMessage(embed.build()).complete()

						storedMessage.content = event.message.contentRaw

						loritta save storedMessage
						return@execute
					}
				}
			}
		}
	}

	override fun onGuildMessageDelete(event: GuildMessageDeleteEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			val config = loritta.getServerConfigForGuild(event.guild.id)
			val locale = loritta.getLocaleById(config.localeId)
			val eventLogConfig = config.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.messageDeleted) {
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId)
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@execute

				if (textChannel != null && textChannel.canTalk()) {
					val storedMessage = loritta.storedMessagesColl.find(Filters.eq("_id", event.messageId)).first()
					if (storedMessage != null) {
						val embed = EmbedBuilder()
						embed.setTimestamp(Instant.now())

						embed.setColor(Color(221, 0, 0))

						embed.setAuthor(storedMessage.authorName, null, null)

						var deletedMessage = "\uD83D\uDCDD ${locale["EVENTLOG_MESSAGE_DELETED", storedMessage.content, "<#${storedMessage.channelId}>"]}"

						if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
							val auditEntry = event.guild.auditLogs.complete().firstOrNull()

							if (auditEntry != null && auditEntry.type == ActionType.MESSAGE_DELETE) {
								if (auditEntry.targetId == storedMessage.authorId) {
									deletedMessage += "\n" + locale["EVENTLOG_MESSAGE_DeletedBy", auditEntry.user?.asMention ?: "???"] + "\n"
								}
							}
						}

						if (storedMessage.attachments != null && storedMessage.attachments.isNotEmpty()) {
							deletedMessage += "\n${locale.get("EVENTLOG_MESSAGE_DELETED_UPLOADS")}\n" + storedMessage.attachments.joinToString(separator = "\n")
						}

						embed.setDescription(deletedMessage)

						textChannel.sendMessage(embed.build()).complete()

						loritta.storedMessagesColl.deleteOne(Filters.eq("_id", event.messageId))
						return@execute
					}
				}
			}
		}
	}

	override fun onGuildVoiceJoin(event: GuildVoiceJoinEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
			val eventLogConfig = serverConfig.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.voiceChannelJoins) {
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return@execute
				val locale = loritta.getLocaleById(serverConfig.localeId)

				if (!textChannel.canTalk())
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@execute

				val embed = EmbedBuilder()
				embed.setTimestamp(Instant.now())

				embed.setColor(Color(35, 209, 96))

				embed.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)
				embed.setDescription("\uD83D\uDC49\uD83C\uDFA4 **${locale["EVENTLOG_JoinedVoiceChannel", event.member.asMention, event.channelJoined.name]}**")
				embed.setFooter(locale["EVENTLOG_USER_ID", event.member.user.id], null)

				textChannel.sendMessage(embed.build()).complete()
				return@execute
			}
		}
	}

	override fun onGuildVoiceLeave(event: GuildVoiceLeaveEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
			val eventLogConfig = serverConfig.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.voiceChannelLeaves) {
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return@execute
				val locale = loritta.getLocaleById(serverConfig.localeId)
				if (!textChannel.canTalk())
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@execute

				val embed = EmbedBuilder()
				embed.setTimestamp(Instant.now())
				embed.setColor(Color(35, 209, 96))

				embed.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)
				embed.setDescription("\uD83D\uDC48\uD83C\uDFA4 **${locale["EVENTLOG_LeftVoiceChannel", event.member.asMention, event.channelLeft.name]}**")
				embed.setFooter(locale["EVENTLOG_USER_ID", event.member.user.id], null)

				textChannel.sendMessage(embed.build()).complete()
				return@execute
			}
		}
	}

	override fun onGuildBan(event: GuildBanEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			// Fazer relay de bans
			if (event.guild.id == "297732013006389252") {
				val relayTo = lorittaShards.getGuildById("420626099257475072")

				if (relayTo != null) {
					if (relayTo.banList.complete().firstOrNull { it.user == event.user } == null) {
						relayTo.controller.ban(event.user, 7, "Banned on LorittaLand (Brazilian Server)")?.queue()
					}
				}
			}
			if (event.guild.id == "420626099257475072") {
				val relayTo = lorittaShards.getGuildById("297732013006389252")

				if (relayTo != null) {
					if (relayTo.banList.complete().firstOrNull { it.user == event.user } == null) {
						relayTo.controller.ban(event.user, 7, "Banido na LorittaLand (English Server)")?.queue()
					}
				}
			}

			val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
			val eventLogConfig = serverConfig.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.memberBanned) {
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return@execute
				val locale = loritta.getLocaleById(serverConfig.localeId)

				if (!textChannel.canTalk())
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@execute

				val embed = EmbedBuilder()
				embed.setTimestamp(Instant.now())
				embed.setColor(Color(35, 209, 96))

				var message = "\uD83D\uDEAB **${locale["EVENTLOG_Banned", event.user.name]}**";

				if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
					// Caso a Loritta consiga ver o audit log, vamos pegar quem baniu e o motivo do ban!
					val auditLog = event.guild.auditLogs.complete().first()

					if (auditLog.type == ActionType.BAN) {
						message += "\n**${locale["BAN_PunishedBy"]}:** ${auditLog.user?.asMention ?: "???"}";
						message += "\n**${locale["BAN_PunishmentReason"]}:** `${if (auditLog.reason == null) "\uD83E\uDD37 Nenhum motivo" else auditLog.reason}`";
					}
				}
				embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
				embed.setDescription(message)
				embed.setFooter(locale["EVENTLOG_USER_ID", event.user.id], null)

				textChannel.sendMessage(embed.build()).complete()
				return@execute
			}
		}
	}

	override fun onGuildUnban(event: GuildUnbanEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			// Fazer relay de unbans
			if (event.guild.id == "297732013006389252") {
				val relayTo = lorittaShards.getGuildById("420626099257475072")

				relayTo?.controller?.unban(event.user)?.queue()
			}
			if (event.guild.id == "420626099257475072") {
				val relayTo = lorittaShards.getGuildById("297732013006389252")

				relayTo?.controller?.unban(event.user)?.queue()
			}

			val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
			val eventLogConfig = serverConfig.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.memberUnbanned) {
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return@execute
				val locale = loritta.getLocaleById(serverConfig.localeId)
				if (!textChannel.canTalk())
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@execute

				val embed = EmbedBuilder()
				embed.setTimestamp(Instant.now())
				embed.setColor(Color(35, 209, 96))

				var message = "\uD83E\uDD1D **${locale["EVENTLOG_Unbanned", event.user.name]}**";

				if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
					// Caso a Loritta consiga ver o audit log, vamos pegar quem baniu e o motivo do ban!
					val auditLog = event.guild.auditLogs.complete().first()

					if (auditLog.type == ActionType.UNBAN) {
						message += "\n${locale["EVENTLOG_UnbannedBy", auditLog.user?.asMention ?: "???"]}"
					}
				}

				embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
				embed.setDescription(message)
				embed.setFooter(locale["EVENTLOG_USER_ID", event.user.id], null)

				textChannel.sendMessage(embed.build()).complete()
				return@execute
			}
		}
	}

	override fun onGuildMemberNickChange(event: GuildMemberNickChangeEvent) {
		if (DebugLog.cancelAllEvents)
			return

		loritta.executor.execute {
			val serverConfig = loritta.getServerConfigForGuild(event.guild.id)
			val eventLogConfig = serverConfig.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.nicknameChanges) {
				val locale = loritta.getLocaleById(serverConfig.localeId)
				val embed = EmbedBuilder()
				embed.setColor(Color(35, 209, 96))
				embed.setTimestamp(Instant.now())
				embed.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)

				// ===[ NICKNAME ]===
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return@execute
				if (!textChannel.canTalk())
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return@execute
				if (!event.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return@execute

				val oldNickname = if (event.prevNick == null) "\uD83E\uDD37 ${locale["EVENTLOG_NoNickname"]}" else event.prevNick
				val newNickname = if (event.newNick == null) "\uD83E\uDD37 ${locale["EVENTLOG_NoNickname"]}" else event.newNick

				embed.setDescription("\uD83D\uDCDD ${locale["EVENTLOG_NicknameChanged", oldNickname, newNickname]}")
				embed.setFooter(locale["EVENTLOG_USER_ID", event.member.user.id], null)

				textChannel.sendMessage(embed.build()).complete()
				return@execute
			}
		}
	}
}