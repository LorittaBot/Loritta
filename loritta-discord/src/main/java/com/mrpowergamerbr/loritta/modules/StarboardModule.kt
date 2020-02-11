package com.mrpowergamerbr.loritta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import com.mrpowergamerbr.loritta.dao.StarboardMessage
import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.tables.StarboardMessages
import com.mrpowergamerbr.loritta.userdata.MongoServerConfig
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.transactions.transaction
import java.awt.Color
import java.util.concurrent.TimeUnit

object StarboardModule {
	private val logger = KotlinLogging.logger {}
	private val mutexes = Caffeine.newBuilder()
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build<Long, Mutex>()
			.asMap()

	suspend fun handleStarboardReaction(e: GenericMessageReactionEvent, serverConfig: MongoServerConfig) {
		// Não enviar mensagens para o starboard se o canal é NSFW
		if (e.textChannel.isNSFW)
			return

		val guild = e.guild
		val starboardConfig = serverConfig.starboardConfig
		val starboardId = starboardConfig.starboardId

		if (e.reactionEmote.isEmote("⭐") && starboardId != null) {
			// Caso não tenha permissão para ver o histórico de mensagens, retorne!
			if (!e.guild.selfMember.hasPermission(e.textChannel, Permission.MESSAGE_HISTORY, Permission.MESSAGE_EMBED_LINKS))
				return

			// Criar um mutex da guild, para evitar que envie várias mensagens iguais ao mesmo tempo
			val mutex = mutexes.getOrPut(e.guild.idLong) { Mutex() }

			mutex.withLock {
				val msg = e.textChannel.retrieveMessageById(e.messageId).await() ?: return@withLock

				val textChannel = guild.getTextChannelById(starboardId) ?: return@withLock

				// Se algum "engracadinho" está enviando reações nas mensagens do starboard, apenas ignore.
				// Também verifique se a Lori pode falar no canal!
				if (textChannel == msg.textChannel || !textChannel.canTalk())
					return@withLock

				val starboardEmbedMessage = transaction(Databases.loritta) {
					StarboardMessage.find {
						StarboardMessages.guildId eq e.guild.idLong and (StarboardMessages.messageId eq e.messageIdLong)
					}.firstOrNull()
				}

				val starboardEmbedMessageId = starboardEmbedMessage?.embedId

				var starboardMessage: Message? = starboardEmbedMessageId?.let {
					try {
						textChannel.retrieveMessageById(starboardEmbedMessageId).await()
					} catch (exception: Exception) {
						logger.error(exception) { "Error while retrieving starboard embed ID $starboardEmbedMessageId from ${e.guild}"}
						null
					}
				}

				val embed = EmbedBuilder()
				val count = e.reaction.retrieveUsers().await().size
				val content = msg.contentRaw

				embed.setAuthor("${msg.author.name}#${msg.author.discriminator} (${msg.author.id})", null, msg.author.effectiveAvatarUrl)
				embed.setTimestamp(msg.timeCreated)
				embed.setColor(Color(255, 255, Math.max(255 - (count * 15), 0)))
				embed.addField("Ir para a mensagem", "[Clique aqui](https://discordapp.com/channels/${msg.guild.id}/${msg.channel.id}/${msg.id})", false)

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
					var fieldValue = ""
					for (attach in msg.attachments) {
						if (attach.isImage && !hasImage) { // Se é uma imagem...
							embed.setImage(attach.url) // Então coloque isso como a imagem no embed!
							hasImage = true
						}
						fieldValue += "\uD83D\uDD17 **|** [${attach.fileName}](${attach.url})\n"
					}
					embed.addField("Arquivos", fieldValue, false)
				}

				embed.setDescription(content)

				val starCountMessage = MessageBuilder()
				starCountMessage.append("$emoji **$count** - ${e.textChannel.asMention}")
				starCountMessage.setEmbed(embed.build())

				if (starboardMessage != null) {
					if (starboardConfig.requiredStars > count) { // Remover embed já que o número de stars é menos que o número necessário de estrelas
						transaction(Databases.loritta) {
							starboardEmbedMessage?.delete() // Remover da database
						}
						starboardMessage.delete().await() // Deletar a embed do canal de starboard
					} else {
						// Editar a mensagem com a nova mensagem!
						starboardMessage.editMessage(starCountMessage.build()).await()
					}
				} else if (count >= starboardConfig.requiredStars) {
					starboardMessage = textChannel.sendMessage(starCountMessage.build()).await()

					transaction(Databases.loritta) {
						StarboardMessage.new {
							this.guildId = e.guild.idLong
							this.embedId = starboardMessage.idLong
							this.messageId = msg.idLong
						}
					}
				}
			}
		}
	}
}