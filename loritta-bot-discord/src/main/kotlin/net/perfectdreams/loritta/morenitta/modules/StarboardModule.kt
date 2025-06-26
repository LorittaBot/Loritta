package net.perfectdreams.loritta.morenitta.modules

import com.github.benmanes.caffeine.cache.Caffeine
import dev.minn.jda.ktx.messages.InlineEmbed
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.sticker.Sticker
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.components.button.Button
import net.dv8tion.jda.api.components.button.ButtonStyle
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.ContentTypeUtils
import net.perfectdreams.loritta.cinnamon.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.pudding.tables.StarboardMessages
import net.perfectdreams.loritta.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.StarboardMessage
import net.perfectdreams.loritta.morenitta.dao.servers.moduleconfigs.StarboardConfig
import net.perfectdreams.loritta.morenitta.utils.extensions.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import java.awt.Color
import java.util.concurrent.TimeUnit

class StarboardModule(val loritta: LorittaBot) {
	companion object {
		const val STAR_REACTION = "⭐"
		private val logger by HarmonyLoggerFactory.logger {}
	}

	private val mutexes = Caffeine.newBuilder()
			.expireAfterAccess(60, TimeUnit.SECONDS)
			.build<Long, Mutex>()
			.asMap()

	suspend fun handleStarboardReaction(i18nContext: I18nContext, e: GenericMessageReactionEvent, starboardConfig: StarboardConfig) {
		val eventTextChannel = if (e.isFromType(ChannelType.TEXT)) e.channel.asTextChannel() else return
		// Não enviar mensagens para o starboard se o canal é NSFW
		if (eventTextChannel.isNSFW)
			return

		val guild = e.guild
		val starboardId = starboardConfig.starboardChannelId

		if (e.emoji.isEmote(STAR_REACTION)) {
			val textChannel = guild.getGuildMessageChannelById(starboardId) ?: return

			// Caso não tenha permissão para ver o histórico de mensagens, retorne!
			if (!e.guild.selfMember.hasPermission(eventTextChannel, Permission.MESSAGE_HISTORY, Permission.MESSAGE_EMBED_LINKS))
				return

			// Criar um mutex da guild, para evitar que envie várias mensagens iguais ao mesmo tempo
			val mutex = mutexes.getOrPut(e.guild.idLong) { Mutex() }

			mutex.withLock {
				val messageThatWasReactedTo = eventTextChannel.retrieveMessageById(e.messageId).await() ?: return@withLock

				// Se algum "engracadinho" está enviando reações nas mensagens do starboard, apenas ignore.
				// Também verifique se a Lori pode falar no canal!
				if (textChannel == messageThatWasReactedTo.textChannel || !textChannel.canTalk())
					return@withLock

				val starboardEmbedMessage = loritta.newSuspendedTransaction {
					StarboardMessage.find {
						StarboardMessages.guildId eq e.guild.idLong and (StarboardMessages.messageId eq messageThatWasReactedTo.idLong)
					}.firstOrNull()
				}

				val starboardEmbedMessageId = starboardEmbedMessage?.embedId

				// Here we are getting the message on the starboard channel
				var starboardMessage: Message? = starboardEmbedMessageId?.let {
					try {
						textChannel.retrieveMessageById(starboardEmbedMessageId).await()
					} catch (exception: Exception) {
						logger.error(exception) { "Error while retrieving starboard embed ID $starboardEmbedMessageId from ${e.guild}"}
						null
					}
				}

				val count = e.reaction.retrieveUsers().await().size
				val emoji = getStarEmojiForReactionCount(count)

				val starCountMessage = MessageCreate {
					this.content = "$emoji **$count** - ${eventTextChannel.asMention}"
					embed(createStarboardEmbed(i18nContext, messageThatWasReactedTo, count))
					actionRow(
						Button.of(ButtonStyle.LINK, messageThatWasReactedTo.jumpUrl, i18nContext.get(I18nKeysData.Modules.Starboard.JumpToMessage))
					)
				}

				if (starboardMessage != null) {
					if (starboardConfig.requiredStars > count) { // Remover embed já que o número de stars é menos que o número necessário de estrelas
						loritta.newSuspendedTransaction {
							starboardEmbedMessage?.delete() // Remover da database
						}
						starboardMessage.delete().await() // Deletar a embed do canal de starboard
					} else {
						// Editar a mensagem com a nova mensagem!
						starboardMessage.editMessage(MessageEditData.fromCreateData(starCountMessage)).await()
					}
				} else if (count >= starboardConfig.requiredStars) {
					starboardMessage = textChannel.sendMessage(starCountMessage).await()

					loritta.newSuspendedTransaction {
						// Delete all previous starboard messages related to this message
						// Avoid issues when, somehow, Loritta keeps resending a new message because she can't query the previous message (maybe it was deleted?)
						StarboardMessages.deleteWhere {
							(StarboardMessages.guildId eq e.guild.idLong) and (StarboardMessages.messageId eq messageThatWasReactedTo.idLong)
						}

						StarboardMessage.new {
							this.guildId = e.guild.idLong
							this.embedId = starboardMessage.idLong
							this.messageId = messageThatWasReactedTo.idLong
						}
					}
				}
			}
		}
	}

	private fun getStarEmojiForReactionCount(count: Int) = when {
		count == 69 -> Emotes.LoriBonk.toString() // Easter Egg
		count >= 20 -> "\uD83C\uDF0C"
		count >= 15 -> "\uD83D\uDCAB"
		count >= 10 -> "\uD83C\uDF20"
		count >= 5 -> "\uD83C\uDF1F"
		else -> STAR_REACTION
	}

	private fun createStarboardEmbed(
		i18nContext: I18nContext,
		message: Message,
		reactionCount: Int,
	): InlineEmbed.() -> (Unit) = {
		author(
			message.author.asGlobalNameOrLegacyTag + " (${message.author.id})",
			null,
			message.author.effectiveAvatarUrl
		)

		// Show the message's attachments in the embed
		if (message.attachments.isNotEmpty()) {
			field(
				"${Emotes.FileFolder} ${i18nContext.get(I18nKeysData.Modules.Starboard.Files(message.attachments.size))}",
				message.attachments.joinToString("\n") {
					"[${it.fileName}](${it.url})"
				}
			)
		}

		// Cut if the message is too long
		description = message.contentRaw.shortenWithEllipsis(2048)

		// Set the embed's image to the first attachment in the message
		image = message.attachments.firstOrNull { it.contentType in ContentTypeUtils.COMMON_IMAGE_CONTENT_TYPES }?.url

		thumbnail = message.stickers.firstOrNull { it.formatType == Sticker.StickerFormat.PNG || it.formatType == Sticker.StickerFormat.APNG || it.formatType == Sticker.StickerFormat.GIF }?.iconUrl

		color = Color(255, 255, (255 - (reactionCount * 15)).coerceAtLeast(0)).rgb
		timestamp = message.timeCreated
	}
}