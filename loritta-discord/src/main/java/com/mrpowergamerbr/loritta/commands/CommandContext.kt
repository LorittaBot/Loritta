package com.mrpowergamerbr.loritta.commands

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookMessage
import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.referenceIfPossible
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.PermissionException
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.NoCopyByteArrayOutputStream
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.utils.DiscordUtils
import net.perfectdreams.loritta.utils.ImageFormat
import net.perfectdreams.loritta.utils.extensions.build
import net.perfectdreams.loritta.utils.extensions.getEffectiveAvatarUrl
import org.jsoup.Jsoup
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.util.*
import javax.imageio.ImageIO

/**
 * Contexto do comando executado
 */
class CommandContext(val config: ServerConfig, var lorittaUser: LorittaUser, val locale: BaseLocale, var event: LorittaMessageEvent, var cmd: AbstractCommand, var args: Array<String>, var rawArgs: Array<String>, var strippedArgs: Array<String>) {
	var metadata = HashMap<String, Any>()

	val isPrivateChannel: Boolean
		get() = event.isFromType(ChannelType.PRIVATE)

	val message: Message
		get() = event.message

	val handle: Member
		get() {
			if (lorittaUser is GuildLorittaUser) {
				return (lorittaUser as GuildLorittaUser).member
			}
			throw RuntimeException("Trying to use getHandle() in LorittaUser!")
		}

	val userHandle: User
		get() = lorittaUser.user

	val asMention: String
		get() = lorittaUser.asMention

	val guild: Guild
		get() = event.guild!!


	suspend fun explain() {
		cmd.explain(this)
	}

	/**
	 * Verifica se o usuário tem permissão para utilizar um comando
	 */
	fun canUseCommand(): Boolean {
		return lorittaUser.canUseCommand(this)
	}

	fun getAsMention(addSpace: Boolean): String {
		return lorittaUser.user.asMention + (if (addSpace) " " else "")
	}

	suspend fun reply(message: String, prefix: String? = null, addInlineReply: Boolean = true, forceMention: Boolean = false): Message {
		var send = ""
		if (prefix != null) {
			send = "$prefix **|** "
		}
		send = send + (if (forceMention) userHandle.asMention + " " else getAsMention(true)) + message
		return sendMessage(send, addInlineReply = addInlineReply)
	}

	suspend fun reply(vararg loriReplies: LorittaReply, addInlineReply: Boolean = true): Message {
		return reply(false, *loriReplies, addInlineReply = addInlineReply)
	}

	suspend fun reply(mentionUserBeforeReplies: Boolean, vararg loriReplies: LorittaReply, addInlineReply: Boolean = true): Message {
		val message = StringBuilder()
		if (mentionUserBeforeReplies) {
			message.append(LorittaReply().build(this))
			message.append("\n")
		}
		for (loriReply in loriReplies) {
			message.append(loriReply.build(this))
			message.append("\n")
		}
		return sendMessage(message.toString(), addInlineReply = addInlineReply)
	}

	suspend fun reply(image: BufferedImage, fileName: String, vararg loriReplies: LorittaReply): Message {
		val message = StringBuilder()
		for (loriReply in loriReplies) {
			message.append(loriReply.build(this) + "\n")
		}
		return sendFile(image, fileName, message.toString())
	}

	suspend fun sendMessage(message: String, addInlineReply: Boolean = true): Message {
		return sendMessage(MessageBuilder()
				.denyMentions(
						Message.MentionType.EVERYONE,
						Message.MentionType.HERE
				)
				.append(if (message.isEmpty()) " " else message)
				.build(),
				addInlineReply = addInlineReply
		)
	}

	suspend fun sendMessage(message: String, embed: MessageEmbed, addInlineReply: Boolean = true): Message {
		return sendMessage(MessageBuilder()
				.denyMentions(
						Message.MentionType.EVERYONE,
						Message.MentionType.HERE
				)
				.setEmbed(embed)
				.append(if (message.isEmpty()) " " else message)
				.build(),
				addInlineReply = addInlineReply
		)
	}

	suspend fun sendMessage(embed: MessageEmbed, addInlineReply: Boolean = true): Message {
		return sendMessage(
				MessageBuilder()
						.denyMentions(
								Message.MentionType.EVERYONE,
								Message.MentionType.HERE
						)
						.append(getAsMention(true))
						.setEmbed(embed)
						.build(),
				addInlineReply = addInlineReply)
	}

	suspend fun sendMessage(message: Message, addInlineReply: Boolean = true): Message {
		if (isPrivateChannel || event.textChannel!!.canTalk()) {
			return event.channel.sendMessage(message)
					.referenceIfPossible(event.message, config, addInlineReply)
					.await()
		} else {
			throw RuntimeException("Sem permissão para enviar uma mensagem!")
		}
	}

	suspend fun sendMessage(webhook: WebhookClient?, message: WebhookMessage, addInlineReply: Boolean = true) {
		if (!isPrivateChannel && webhook != null) { // Se a webhook é diferente de null, então use a nossa webhook disponível!
			webhook.send(message)
		} else { // Se não, iremos usar embeds mesmo...
			val builder = EmbedBuilder()
			builder.setAuthor(message.username, null, message.avatarUrl)
			builder.setDescription(message.content)
			builder.setFooter("Não consigo usar as permissões de webhook aqui... então estou usando o modo de pobre!", null)

			sendMessage(builder.build(), addInlineReply = addInlineReply)
		}
	}

	suspend fun sendFile(file: File, name: String, message: String, embed: MessageEmbed? = null): Message {
		// Corrigir erro ao construir uma mensagem vazia
		val builder = MessageBuilder()
				.denyMentions(
						Message.MentionType.EVERYONE,
						Message.MentionType.HERE
				)
		builder.append(if (message.isEmpty()) " " else message)
		if (embed != null)
			builder.setEmbed(embed)
		return sendFile(file, name, builder.build())
	}

	suspend fun sendFile(file: File, name: String, message: Message): Message {
		val inputStream = file.inputStream()
		return sendFile(inputStream, name, message)
	}

	suspend fun sendFile(image: BufferedImage, name: String, embed: MessageEmbed): Message {
		return sendFile(image, name, "", embed)
	}

	suspend fun sendFile(image: BufferedImage, name: String, message: String, embed: MessageEmbed? = null): Message {
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		if (embed != null)
			builder.setEmbed(embed)

		return sendFile(image, name, builder.build())
	}

	suspend fun sendFile(image: BufferedImage, name: String, message: Message): Message {
		val output = NoCopyByteArrayOutputStream()

		ImageIO.write(image, "png", output)

		val inputStream = ByteArrayInputStream(output.toByteArray(), 0, output.size())

		return sendFile(inputStream, name, message)
	}

	suspend fun sendFile(inputStream: InputStream, name: String, message: String): Message {
		// Corrigir erro ao construir uma mensagem vazia
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		return sendFile(inputStream, name, builder.build())
	}

	suspend fun sendFile(inputStream: InputStream, name: String, embed: MessageEmbed): Message {
		return sendFile(inputStream, name, "", embed)
	}

	suspend fun sendFile(inputStream: InputStream, name: String, message: String, embed: MessageEmbed? = null): Message {
		// Corrigir erro ao construir uma mensagem vazia
		val builder = MessageBuilder()
				.denyMentions(
						Message.MentionType.EVERYONE,
						Message.MentionType.HERE
				)
		builder.append(if (message.isEmpty()) " " else message)
		if (embed != null)
			builder.setEmbed(embed)
		return sendFile(inputStream, name, builder.build())
	}

	suspend fun sendFile(inputStream: InputStream, name: String, message: Message): Message {
		if (isPrivateChannel || event.textChannel!!.canTalk()) {
			val sentMessage = event.channel.sendMessage(message)
					.addFile(inputStream, name)
					.referenceIfPossible(event.message, config, true)
					.await()
			return sentMessage
		} else {
			throw RuntimeException("Sem permissão para enviar uma mensagem!")
		}
	}

	/**
	 * Gets an user from the argument index via mentions, username#oldDiscriminator, effective name, username and user ID
	 *
	 * @param argument the argument index on the rawArgs array
	 * @return         the user object or null, if nothing was found
	 * @see            User
	 */
	suspend fun getUserAt(argument: Int) = this.rawArgs.getOrNull(argument)
			?.let {
				DiscordUtils.extractUserFromString(
						it,
						message.mentionedUsers,
						if (isPrivateChannel) null else guild
				)
			}

	/**
	 * Gets an image URL from the argument index via valid URLs at the specified index
	 *
	 * @param argument   the argument index on the rawArgs array
	 * @param search     how many messages will be retrieved from the past to get images (default: 25)
	 * @param avatarSize the size of retrieved user avatars from Discord (default: 2048)
	 * @return           the image URL or null, if nothing was found
	 */
	suspend fun getImageUrlAt(argument: Int, search: Int = 25, avatarSize: Int = 256): String? {
		if (this.rawArgs.size > argument) { // Primeiro iremos verificar se existe uma imagem no argumento especificado
			val link = this.rawArgs[argument] // Ok, será que isto é uma URL?

			if (LorittaUtils.isValidUrl(link) && loritta.connectionManager.isTrusted(link))
				return link // Se é um link, vamos enviar para o usuário agora

			// Vamos verificar por usuários no argumento especificado
			val user = getUserAt(argument)
			if (user != null)
				return user.getEffectiveAvatarUrl(ImageFormat.PNG, avatarSize)

			// Ainda não?!? Vamos verificar se é um emoji.
			// Um emoji custom do Discord é + ou - assim: <:loritta:324931508542504973>
			for (emote in this.message.emotes) {
				if (link.equals(emote.asMention, ignoreCase = true)) {
					return emote.imageUrl
				}
			}

			for (embed in this.message.embeds) {
				if (embed.image != null && loritta.connectionManager.isTrusted(embed.image!!.url!!))
					return embed.image!!.url
			}
			for (attachment in this.message.attachments) {
				if (attachment.isImage && loritta.connectionManager.isTrusted(attachment.url))
					return attachment.url
			}

			// Se não é nada... então talvez seja um emoji padrão do Discordão!
			// Na verdade é um emoji padrão...
			try {
				var unicodeEmoji = LorittaUtils.toUnicode(this.rawArgs[argument].codePointAt(0)) // Vamos usar codepoints porque emojis
				unicodeEmoji = unicodeEmoji.substring(2) // Remover coisas desnecessárias
				val toBeDownloaded = "https://twemoji.maxcdn.com/2/72x72/$unicodeEmoji.png"
				if (HttpRequest.get(toBeDownloaded).code() == 200) {
					return toBeDownloaded
				}
			} catch (e: Exception) {
			}
		}

		// Nothing found? Try retrieving the replied message content
		if (!this.isPrivateChannel && this.guild.selfMember.hasPermission(this.event.textChannel!!, Permission.MESSAGE_HISTORY)) {
			val referencedMessage = message.referencedMessage
			if (referencedMessage != null) {
				for (embed in referencedMessage.embeds) {
					if (embed.image != null && loritta.connectionManager.isTrusted(embed.image!!.url!!))
						return embed.image!!.url
				}
				for (attachment in referencedMessage.attachments) {
					if (attachment.isImage && loritta.connectionManager.isTrusted(attachment.url))
						return attachment.url
				}
			}
		}

		// Ainda nada válido? Quer saber, desisto! Vamos pesquisar as mensagens antigas deste servidor & embeds então para encontrar attachments...
		if (search > 0 && !this.isPrivateChannel && this.guild.selfMember.hasPermission(this.event.textChannel!!, Permission.MESSAGE_HISTORY)) {
			try {
				val message = this.message.channel.history.retrievePast(search).await()

				attach@ for (msg in message) {
					for (embed in msg.embeds) {
						if (embed.image != null && loritta.connectionManager.isTrusted(embed.image!!.url!!)) {
							return embed.image!!.url
						}
					}
					for (attachment in msg.attachments) {
						if (attachment.isImage && loritta.connectionManager.isTrusted(attachment.url)) {
							return attachment.url
						}
					}
				}
			} catch (e: PermissionException) {
			}

		}

		return null
	}

	/**
	 * Gets an image from the argument index via valid URLs at the specified index
	 *
	 * @param argument   the argument index on the rawArgs array
	 * @param search     how many messages will be retrieved from the past to get images (default: 25)
	 * @param avatarSize the size of retrieved user avatars from Discord (default: 2048)
	 * @return           the image object or null, if nothing was found
	 * @see              BufferedImage
	 */
	suspend fun getImageAt(argument: Int, search: Int = 25, avatarSize: Int = 256, createTextAsImageIfNotFound: Boolean = true): BufferedImage? {
		var toBeDownloaded = getImageUrlAt(argument, 0, avatarSize)

		if (toBeDownloaded == null) {
			if (rawArgs.isNotEmpty() && createTextAsImageIfNotFound) {
				val textForTheImage = rawArgs.drop(argument)
						.joinToString(" ")

				if (textForTheImage.isNotBlank())
					return ImageUtils.createTextAsImage(256, 256, rawArgs.drop(argument).joinToString(" "))
			}

			if (search != 0) {
				toBeDownloaded = getImageUrlAt(argument, search, avatarSize)
			}
		}

		if (toBeDownloaded == null)
			return null

		// Vamos baixar a imagem!
		try {
			// Workaround para imagens do prnt.scr/prntscr.com (mesmo que o Lightshot seja um lixo)
			if (toBeDownloaded.contains("prnt.sc") || toBeDownloaded.contains("prntscr.com")) {
				val document = Jsoup.connect(toBeDownloaded).get()
				val elements = document.getElementsByAttributeValue("property", "og:image")
				if (!elements.isEmpty()) {
					toBeDownloaded = elements.attr("content")
				}
			}
			return LorittaUtils.downloadImage(toBeDownloaded ?: return null)
		} catch (e: Exception) {
			return null
		}
	}
}
