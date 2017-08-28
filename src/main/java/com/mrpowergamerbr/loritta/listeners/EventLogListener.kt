package com.mrpowergamerbr.loritta.listeners

import com.google.common.cache.CacheBuilder
import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.userdata.LorittaProfile
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.eventlog.StoredMessage
import com.mrpowergamerbr.loritta.utils.msgFormat
import com.mrpowergamerbr.loritta.utils.save
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
import net.dv8tion.jda.core.events.guild.GenericGuildEvent
import net.dv8tion.jda.core.events.guild.GuildBanEvent
import net.dv8tion.jda.core.events.guild.GuildUnbanEvent
import net.dv8tion.jda.core.events.guild.member.GenericGuildMemberEvent
import net.dv8tion.jda.core.events.guild.member.GuildMemberNickChangeEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceJoinEvent
import net.dv8tion.jda.core.events.guild.voice.GuildVoiceLeaveEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageDeleteEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageReceivedEvent
import net.dv8tion.jda.core.events.message.guild.GuildMessageUpdateEvent
import net.dv8tion.jda.core.events.user.GenericUserEvent
import net.dv8tion.jda.core.events.user.UserAvatarUpdateEvent
import net.dv8tion.jda.core.events.user.UserNameUpdateEvent
import net.dv8tion.jda.core.hooks.ListenerAdapter
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import kotlin.concurrent.thread

class EventLogListener(internal val loritta: Loritta) : ListenerAdapter() {
	val handledUsernameChanges = CacheBuilder.newBuilder().expireAfterWrite(15, TimeUnit.SECONDS).maximumSize(100).build<Any, Any>().asMap()

	// ===[ EVENT LOG ]===
	// Users
	override fun onGenericUser(event: GenericUserEvent) {
		thread {
			// Atualizar coisas como user é mais difícil
			val embed = EmbedBuilder()
			embed.setTimestamp(Instant.now())
			embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
			embed.setColor(Color(114, 137, 218))
			embed.setImage("attachment://avatar.png")

			// Atualizar avatar
			if (event is UserAvatarUpdateEvent) {
				// Primeiro iremos criar a imagem do update
				val rawOldAvatar = LorittaUtils.downloadImage(if (event.previousAvatarUrl == null) event.user.defaultAvatarUrl else event.previousAvatarUrl.replace("jpg", "png"))
				val rawNewAvatar = LorittaUtils.downloadImage(event.user.effectiveAvatarUrl.replace("jpg", "png"))

				if (rawOldAvatar == null || rawNewAvatar == null) // As vezes o avatar pode ser null
					return@thread

				val oldAvatar = rawOldAvatar.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH)
				val newAvatar = rawNewAvatar.getScaledInstance(128, 128, BufferedImage.SCALE_SMOOTH)

				val base = BufferedImage(256, 128, BufferedImage.TYPE_INT_ARGB_PRE)
				val graphics = base.graphics
				graphics.drawImage(oldAvatar, 0, 0, null)
				graphics.drawImage(newAvatar, 128, 0, null)

				val os = ByteArrayOutputStream()
				ImageIO.write(base, "png", os)

				val inputStream = ByteArrayInputStream(os.toByteArray())

				// E agora nós iremos anunciar a troca para todos os servidores
				for (guild in event.jda.guilds) { // Só pegar as guilds desta shard
					if (guild.isMember(event.user)) { // ...desde que o membro esteja no servidor!
						val config = loritta.getServerConfigForGuild(guild.id)
						val locale = loritta.getLocaleById(config.localeId)

						if (config.eventLogConfig.avatarChanges && config.eventLogConfig.isEnabled) {
							val textChannel = guild.getTextChannelById(config.eventLogConfig.eventLogChannelId);

							if (textChannel != null && textChannel.canTalk()) {
								embed.setDescription("\uD83D\uDDBC **${locale.EVENTLOG_AVATAR_CHANGED.msgFormat(event.user.asMention)}**")
								embed.setFooter(locale.EVENTLOG_USER_ID.msgFormat(event.user.id), null)

								val message = MessageBuilder().append(" ").setEmbed(embed.build())

								textChannel.sendFile(inputStream, "avatar.png", message.build()).complete()
							}
						}
					}
				}
				return@thread
			}
			// Atualizar nome
			if (event is UserNameUpdateEvent) {
				// Antes nós iremos salvar o nome velho do usuário no profile dele
				if (!handledUsernameChanges.containsKey(event.user.id)) {
					// É necessário fazer isto já que todas as shards irão receber a notificação de username change
					handledUsernameChanges.put(event.user.id, System.currentTimeMillis())
					val newName = event.user.name
					val newDiscriminator = event.user.discriminator
					val changedAt = System.currentTimeMillis()

					val changeWrapper = LorittaProfile.UsernameChange(changedAt, newName, newDiscriminator)

					val profile = loritta.getLorittaProfileForUser(event.user.id)

					profile.usernameChanges.add(changeWrapper)

					loritta save profile
				}

				// E agora nós iremos anunciar a troca para todos os servidores
				for (guild in event.jda.guilds) {
					if (guild.isMember(event.user)) { // ...desde que o membro esteja no servidor!
						val config = loritta.getServerConfigForGuild(guild.id)
						val locale = loritta.getLocaleById(config.localeId)

						if (config.eventLogConfig.usernameChanges && config.eventLogConfig.isEnabled) {
							val textChannel = guild.getTextChannelById(config.eventLogConfig.eventLogChannelId);

							if (textChannel != null && textChannel.canTalk()) {
								embed.setDescription("\uD83D\uDCDD **${locale.EVENTLOG_NAME_CHANGED.msgFormat(event.user.asMention, "${event.oldName}#${event.oldDiscriminator}", "${event.user.name}#${event.user.discriminator}")}**")
								embed.setFooter(locale.EVENTLOG_USER_ID.msgFormat(event.user.id), null)

								textChannel.sendMessage(embed.build()).complete()
							}
						}
					}
				}
			}
		}
	}

	// TEXT CHANNEL
	override fun onGenericTextChannel(event: GenericTextChannelEvent) {
		thread {
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
					if (event is TextChannelCreateEvent && eventLogConfig.channelCreated) {
						embed.setDescription("\uD83C\uDF1F ${locale.EVENTLOG_CHANNEL_CREATED.msgFormat(event.channel.asMention)}")

						textChannel.sendMessage(embed.build()).complete()
						return@thread
					}
					if (event is TextChannelUpdateNameEvent && eventLogConfig.channelNameUpdated) {
						embed.setDescription("\uD83D\uDCDD ${locale.EVENTLOG_CHANNEL_NAME_UPDATED.msgFormat(event.channel.asMention, event.oldName, event.channel.name)}")

						textChannel.sendMessage(embed.build()).complete()
						return@thread
					}
					if (event is TextChannelUpdateTopicEvent && eventLogConfig.channelTopicUpdated) {
						embed.setDescription("\uD83D\uDCDD ${locale.EVENTLOG_CHANNEL_TOPIC_UPDATED.msgFormat(event.channel.asMention, event.oldTopic, event.channel.topic)}")

						textChannel.sendMessage(embed.build()).complete()
						return@thread
					}
					if (event is TextChannelUpdatePositionEvent && eventLogConfig.channelPositionUpdated) {
						embed.setDescription("\uD83D\uDCDD ${locale.EVENTLOG_CHANNEL_POSITION_UPDATED.msgFormat(event.channel.asMention, event.oldPosition, event.channel.position)}")

						textChannel.sendMessage(embed.build()).complete()
						return@thread
					}
					if (event is TextChannelDeleteEvent && eventLogConfig.channelDeleted) {
						embed.setDescription("\uD83D\uDEAE ${locale.EVENTLOG_CHANNEL_DELETED.msgFormat(event.channel.name)}")

						textChannel.sendMessage(embed.build()).complete()
						return@thread
					}
				}
			}
		}
	}

	// Mensagens
	override fun onGuildMessageReceived(event: GuildMessageReceivedEvent) {
		thread {
			val eventLogConfig = loritta.getServerConfigForGuild(event.guild.id).eventLogConfig

			if (eventLogConfig.isEnabled) {
				loritta save StoredMessage(event.message.id, event.author.name + "#" + event.author.discriminator, event.message.rawContent)
			}
		}
	}

	override fun onGuildMessageUpdate(event: GuildMessageUpdateEvent) {
		thread {
			val config = loritta.getServerConfigForGuild(event.guild.id)
			val locale = loritta.getLocaleById(config.localeId)
			val eventLogConfig = config.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.messageEdit) {
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId);
				if (textChannel != null && textChannel.canTalk()) {
					val storedMessageDocument = loritta.mongo.getDatabase("loritta").getCollection("storedmessages").find(Filters.eq("_id", event.messageId)).first()
					if (storedMessageDocument != null) {
						val oldMessage = loritta.ds.get(StoredMessage::class.java, storedMessageDocument["_id"])
						val embed = EmbedBuilder()
						embed.setTimestamp(Instant.now())

						embed.setColor(Color(35, 209, 96))

						embed.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)
						embed.setDescription("\uD83D\uDCDD ${locale.get("EVENTLOG_MESSAGE_EDITED", event.member.asMention, oldMessage.content, event.message.rawContent)}")
						embed.setFooter(locale.get("EVENTLOG_USER_ID", event.member.user.id), null)

						textChannel.sendMessage(embed.build()).complete()

						oldMessage.content = event.message.rawContent

						loritta save oldMessage
						return@thread
					}
				}
			}
		}
	}

	override fun onGuildMessageDelete(event: GuildMessageDeleteEvent) {
		thread {
			val config = loritta.getServerConfigForGuild(event.guild.id)
			val locale = loritta.getLocaleById(config.localeId)
			val eventLogConfig = config.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.messageDeleted) {
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId)

				if (textChannel != null && textChannel.canTalk()) {
					val storedMessageDocument = loritta.mongo.getDatabase("loritta").getCollection("storedmessages").find(Filters.eq("_id", event.messageId)).first()
					if (storedMessageDocument != null) {
						val oldMessage = loritta.ds.get(StoredMessage::class.java, storedMessageDocument["_id"])
						val embed = EmbedBuilder()
						embed.setTimestamp(Instant.now())

						embed.setColor(Color(35, 209, 96))

						embed.setAuthor(oldMessage.authorName, null, null)
						embed.setDescription("\uD83D\uDCDD ${locale.get("EVENTLOG_MESSAGE_DELETED", oldMessage.content)}")
						// embed.setFooter(locale.get("EVENTLOG_USER_ID", event.member.user.id), null)

						textChannel.sendMessage(embed.build()).complete()

						loritta.mongo.getDatabase("loritta").getCollection("storedmessages").deleteOne(Filters.eq("_id", event.messageId))
						return@thread
					}
				}
			}
		}
	}

	// Guilds
	override fun onGenericGuild(event: GenericGuildEvent) {
		thread {
			val eventLogConfig = loritta.getServerConfigForGuild(event.guild.id).eventLogConfig
			if (eventLogConfig.isEnabled) {
				val textChannel = event.guild.getTextChannelById(eventLogConfig.eventLogChannelId);

				if (textChannel != null && textChannel.canTalk()) {
					val embed = EmbedBuilder()
					embed.setTimestamp(Instant.now())

					// ===[ VOICE JOIN ]===
					if (event is GuildVoiceJoinEvent && eventLogConfig.voiceChannelJoins) {
						embed.setColor(Color(35, 209, 96))

						embed.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)
						embed.setDescription("\uD83D\uDC49\uD83C\uDFA4 **${event.member.asMention} entrou no canal de voz `${event.channelJoined.name}`**")
						embed.setFooter("ID do usuário: ${event.member.user.id}", null)

						textChannel.sendMessage(embed.build()).complete()
						return@thread;
					}
					// ===[ VOICE LEAVE ]===
					if (event is GuildVoiceLeaveEvent && eventLogConfig.voiceChannelLeaves) {
						embed.setColor(Color(35, 209, 96))

						embed.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)
						embed.setDescription("\uD83D\uDC48\uD83C\uDFA4 **${event.member.asMention} saiu do canal de voz `${event.channelLeft.name}`**")
						embed.setFooter("ID do usuário: ${event.member.user.id}", null)

						textChannel.sendMessage(embed.build()).complete()
						return@thread;
					}
					// ===[ USER BANNED ]===
					if (event is GuildBanEvent && eventLogConfig.memberBanned) {
						embed.setColor(Color(35, 209, 96))

						var message = "\uD83D\uDEAB **${event.user.name} foi banido!**";

						if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
							// Caso a Loritta consiga ver o audit log, vamos pegar quem baniu e o motivo do ban!
							val auditLog = event.guild.auditLogs.complete().first()

							if (auditLog.type == ActionType.BAN) {
								message += "\n**Banido por:** ${auditLog.user.asMention}";
								message += "\n**Motivo:** `${if (auditLog.reason == null) "\uD83E\uDD37 Nenhum motivo" else auditLog.reason}`";
							}
						}
						embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
						embed.setDescription(message)
						embed.setFooter("ID do usuário: ${event.user.id}", null)

						textChannel.sendMessage(embed.build()).complete()
						return@thread;
					}
					// ===[ USER UNBANNED ]===
					if (event is GuildUnbanEvent && eventLogConfig.memberUnbanned) {
						embed.setColor(Color(35, 209, 96))

						var message = "\uD83E\uDD1D **${event.user.name} foi desbanido!**";

						if (event.guild.selfMember.hasPermission(Permission.VIEW_AUDIT_LOGS)) {
							// Caso a Loritta consiga ver o audit log, vamos pegar quem baniu e o motivo do ban!
							val auditLog = event.guild.auditLogs.complete().first()

							if (auditLog.type == ActionType.UNBAN) {
								message += "\n**Desbanido por:** ${auditLog.user.asMention}";
							}
						}
						embed.setAuthor("${event.user.name}#${event.user.discriminator}", null, event.user.effectiveAvatarUrl)
						embed.setDescription(message)
						embed.setFooter("ID do usuário: ${event.user.id}", null)

						textChannel.sendMessage(embed.build()).complete()
						return@thread;
					}
					// ===[ GENERIC MEMBER EVENT ]===
					if (event is GenericGuildMemberEvent) {
						embed.setColor(Color(35, 209, 96))

						embed.setAuthor("${event.member.user.name}#${event.member.user.discriminator}", null, event.member.user.effectiveAvatarUrl)

						// ===[ NICKNAME ]===
						if (event is GuildMemberNickChangeEvent && eventLogConfig.nicknameChanges) {
							embed.setDescription("\uD83D\uDCDD **Nickname de ${event.member.asMention} foi alterado!\n\nAntigo nickname: `${if (event.prevNick == null) "\uD83E\uDD37 Nenhum nickname" else event.prevNick}`\nNovo nickname: `${if (event.newNick == null) "\uD83E\uDD37 Nenhum nickname" else event.newNick}`**")
							embed.setFooter("ID do usuário: ${event.member.user.id}", null)

							textChannel.sendMessage(embed.build()).complete()
							return@thread;
						}
					}
				}
			}
		}
	}
}