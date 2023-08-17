package net.perfectdreams.loritta.morenitta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import net.perfectdreams.loritta.morenitta.dao.StarboardMessage
import net.perfectdreams.loritta.cinnamon.pudding.tables.StarboardMessages
import net.perfectdreams.loritta.morenitta.utils.extensions.await
import net.perfectdreams.loritta.morenitta.utils.extensions.isEmote
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.StarboardConfig
import net.perfectdreams.loritta.morenitta.utils.extensions.getGuildMessageChannelById
import net.perfectdreams.loritta.morenitta.utils.extensions.textChannel
import org.jetbrains.exposed.sql.and
import java.awt.Color
import java.util.concurrent.TimeUnit

class StarboardModule(val loritta: LorittaBot) {
	private val logger = KotlinLogging.logger {}
	private val mutexes = Caffeine.newBuilder()
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build<Long, Mutex>()
			.asMap()

	suspend fun handleStarboardReaction(e: GenericMessageReactionEvent, starboardConfig: StarboardConfig) {
		// Não enviar mensagens para o starboard se o canal é NSFW
		val eventTextChannel = if (e.isFromType(ChannelType.TEXT)) e.channel.asTextChannel() else return
		if (eventTextChannel.isNSFW)
			return

		val guild = e.guild
		val starboardId = starboardConfig.starboardChannelId

		if (e.emoji.isEmote("⭐")) {
			val textChannel = guild.getGuildMessageChannelById(starboardId) ?: return

			// Caso não tenha permissão para ver o histórico de mensagens, retorne!
			if (!e.guild.selfMember.hasPermission(eventTextChannel, Permission.MESSAGE_HISTORY, Permission.MESSAGE_EMBED_LINKS))
				return

			// Criar um mutex da guild, para evitar que envie várias mensagens iguais ao mesmo tempo
			val mutex = mutexes.getOrPut(e.guild.idLong) { Mutex() }

			mutex.withLock {
				val msg = eventTextChannel.retrieveMessageById(e.messageId).await() ?: return@withLock

				// Se algum "engracadinho" está enviando reações nas mensagens do starboard, apenas ignore.
				// Também verifique se a Lori pode falar no canal!
				if (textChannel == msg.textChannel || !textChannel.canTalk())
					return@withLock

				val starboardEmbedMessage = loritta.newSuspendedTransaction {
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

				val starCountMessage = MessageCreateBuilder()
				starCountMessage.addContent("$emoji **$count** - ${eventTextChannel.asMention}")
				starCountMessage.setEmbeds(embed.build())

				if (starboardMessage != null) {
					if (starboardConfig.requiredStars > count) { // Remover embed já que o número de stars é menos que o número necessário de estrelas
						loritta.newSuspendedTransaction {
							starboardEmbedMessage?.delete() // Remover da database
						}
						starboardMessage.delete().await() // Deletar a embed do canal de starboard
					} else {
						// Editar a mensagem com a nova mensagem!
						starboardMessage.editMessage(MessageEditData.fromCreateData(starCountMessage.build())).await()
					}
				} else if (count >= starboardConfig.requiredStars) {
					starboardMessage = textChannel.sendMessage(starCountMessage.build()).await()

					loritta.newSuspendedTransaction {
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