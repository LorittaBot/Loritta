package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.loritta
import com.mrpowergamerbr.loritta.utils.save
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import java.awt.Color
import java.util.concurrent.TimeUnit

object StarboardModule {
	private val mutexes = Caffeine.newBuilder()
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build<Long, Mutex>()
			.asMap()

	suspend fun handleStarboardReaction(e: GenericMessageReactionEvent, serverConfig: MongoServerConfig) {
		val guild = e.guild
		val starboardConfig = serverConfig.starboardConfig

		if (e.reactionEmote.name == "⭐") {
			// Caso não tenha permissão para ver o histórico de mensagens, retorne!
			if (!e.guild.selfMember.hasPermission(e.textChannel, Permission.MESSAGE_HISTORY))
				return

			val mutex = mutexes.getOrPut(e.textChannel.idLong) { Mutex() }
			mutex.withLock {
				val msg = e.textChannel.retrieveMessageById(e.messageId).await()
				if (msg != null) {
					val textChannel = guild.getTextChannelById(starboardConfig.starboardId)

					if (textChannel != null && msg.textChannel != textChannel && textChannel.canTalk()) { // Verificar se não é null e verificar se a reaction não foi na starboard
						var starboardMessageId = serverConfig.starboardEmbedMessages.firstOrNull { it.messageId == e.messageId }?.embedId
						var starboardMessage: Message? = null
						if (starboardMessageId != null) {
							starboardMessage = textChannel.retrieveMessageById(starboardMessageId).await()
						}

						val embed = EmbedBuilder()
						val count = e.reaction.users.await().size
						var content = msg.contentRaw
						embed.setAuthor(msg.author.name, null, msg.author.effectiveAvatarUrl)
						embed.setTimestamp(msg.creationTime)
						embed.setColor(Color(255, 255, Math.max(200 - (count * 20), 0)))

						var emoji = "⭐"

						if (count >= 5) {
							emoji = "\uD83C\uDF1F"
						}
						if (count >= 10) {
							emoji = "\uD83C\uDF20"
						}
						if (count >= 15) {
							emoji = "\uD83D\uDCAB"
						}
						if (count >= 20) {
							emoji = "\uD83C\uDF0C"
						}

						var hasImage = false
						if (msg.attachments.isNotEmpty()) { // Se tem attachments...
							content += "\n**Arquivos:**\n"
							for (attach in msg.attachments) {
								if (attach.isImage && !hasImage) { // Se é uma imagem...
									embed.setImage(attach.url) // Então coloque isso como a imagem no embed!
									hasImage = true
								}
								content += attach.url + "\n"
							}
						}

						embed.setDescription(content)

						val starCountMessage = MessageBuilder()
						starCountMessage.append("$emoji **${count}** ${e.textChannel.asMention}")
						starCountMessage.setEmbed(embed.build())

						if (starboardMessage != null) {
							if (starboardConfig.requiredStars > count) { // Remover embed já que o número de stars é menos que 0
								starboardMessage.delete().queue()
								serverConfig.starboardEmbedMessages.removeIf { it.embedId == starboardMessage!!.id }
								loritta save serverConfig
								return
							}
							starboardMessage.editMessage(starCountMessage.build()).await()
						} else if (count >= starboardConfig.requiredStars) {
							starboardMessage = textChannel.sendMessage(starCountMessage.build()).await()
						}
						if (starboardMessage != null) {
							serverConfig.starboardEmbedMessages.add(MongoServerConfig.StarboardMessage(starboardMessage.id, msg.id))
							loritta save serverConfig
						}
					}
				}
			}
		}
	}
}