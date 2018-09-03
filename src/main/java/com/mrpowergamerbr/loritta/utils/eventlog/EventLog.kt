package com.mrpowergamerbr.loritta.utils.eventlog

import com.mongodb.client.model.Filters
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.misc.PomfUtils
import com.mrpowergamerbr.loritta.utils.save
import mu.KotlinLogging
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.VoiceChannel
import org.bson.Document
import java.awt.Color
import java.net.URL
import java.time.Instant

object EventLog {
	val logger = KotlinLogging.logger {}

	fun onMessageReceived(serverConfig: ServerConfig, message: Message) {
		try {
			val eventLogConfig = serverConfig.eventLogConfig

			if (eventLogConfig.isEnabled && (eventLogConfig.messageDeleted || eventLogConfig.messageEdit)) {
				val attachments = mutableListOf<String>()

				message.attachments.forEach {
					attachments.add(it.url)
				}

				loritta save StoredMessage(message.id, message.author.name + "#" + message.author.discriminator, message.contentRaw, message.author.id, message.channel.id, attachments)

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
							Filters.eq("_id", message.id),
							Document("\$set", Document("attachments", reuploadedAttachments))
					)
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao salvar mensagem do event log" }
		}
	}

	fun onMessageUpdate(serverConfig: ServerConfig, locale: BaseLocale, message: Message) {
		val eventLogConfig = serverConfig.eventLogConfig

		try {
			if (eventLogConfig.isEnabled && (eventLogConfig.messageEdit || eventLogConfig.messageDeleted)) {
				val textChannel = message.guild.getTextChannelById(eventLogConfig.eventLogChannelId)
				if (textChannel != null && textChannel.canTalk()) {
					if (!message.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
						return
					if (!message.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
						return
					if (!message.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
						return

					val storedMessage = loritta.storedMessagesColl.find(Filters.eq("_id", message.id)).first()
					if (storedMessage != null && storedMessage.content != message.contentRaw) {
						val embed = EmbedBuilder()
						embed.setTimestamp(Instant.now())

						embed.setColor(Color(238, 241, 0))

						embed.setAuthor("${message.member.user.name}#${message.member.user.discriminator}", null, message.member.user.effectiveAvatarUrl)
						embed.setDescription("\uD83D\uDCDD ${locale["EVENTLOG_MESSAGE_EDITED", message.member.asMention, storedMessage.content, message.contentRaw, message.textChannel.asMention]}")
						embed.setFooter(locale["EVENTLOG_USER_ID", message.member.user.id], null)

						textChannel.sendMessage(embed.build()).queue()

						storedMessage.content = message.contentRaw

						loritta save storedMessage
					}
				}
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao atualizar mensagem do event log" }
		}
	}

	fun onVoiceJoin(serverConfig: ServerConfig, member: Member, channelJoined: VoiceChannel) {
		try {
			val eventLogConfig = serverConfig.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.voiceChannelJoins) {
				val textChannel = member.guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return
				val locale = loritta.getLocaleById(serverConfig.localeId)

				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return

				val embed = EmbedBuilder()
				embed.setTimestamp(Instant.now())

				embed.setColor(Color(35, 209, 96))

				embed.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
				embed.setDescription("\uD83D\uDC49\uD83C\uDFA4 **${locale["EVENTLOG_JoinedVoiceChannel", member.asMention, channelJoined.name]}**")
				embed.setFooter(locale["EVENTLOG_USER_ID", member.user.id], null)

				textChannel.sendMessage(embed.build()).queue()
				return
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao entrar no canal de voz do event log" }
		}
	}

	fun onVoiceLeave(serverConfig: ServerConfig, member: Member, channelLeft: VoiceChannel) {
		try {
			val eventLogConfig = serverConfig.eventLogConfig

			if (eventLogConfig.isEnabled && eventLogConfig.voiceChannelLeaves) {
				val textChannel = member.guild.getTextChannelById(eventLogConfig.eventLogChannelId) ?: return
				val locale = loritta.getLocaleById(serverConfig.localeId)
				if (!textChannel.canTalk())
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_EMBED_LINKS))
					return
				if (!member.guild.selfMember.hasPermission(Permission.VIEW_CHANNEL))
					return
				if (!member.guild.selfMember.hasPermission(Permission.MESSAGE_READ))
					return

				val embed = EmbedBuilder()
				embed.setTimestamp(Instant.now())
				embed.setColor(Color(35, 209, 96))

				embed.setAuthor("${member.user.name}#${member.user.discriminator}", null, member.user.effectiveAvatarUrl)
				embed.setDescription("\uD83D\uDC48\uD83C\uDFA4 **${locale["EVENTLOG_LeftVoiceChannel", member.asMention, channelLeft.name]}**")
				embed.setFooter(locale["EVENTLOG_USER_ID", member.user.id], null)

				textChannel.sendMessage(embed.build()).queue()
			}
		} catch (e: Exception) {
			logger.error(e) { "Erro ao sair do canal de voz do event log" }
		}
	}
}