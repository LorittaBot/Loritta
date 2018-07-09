package com.mrpowergamerbr.loritta.commands

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.vanilla.misc.AjudaCommand
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.*
import net.dv8tion.jda.core.exceptions.PermissionException
import org.jsoup.Jsoup
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.util.*
import javax.imageio.ImageIO

/**
 * Contexto do comando executado
 */
class CommandContext(val config: ServerConfig, var lorittaUser: LorittaUser, locale: BaseLocale, var event: LorittaMessageEvent, var cmd: AbstractCommand, var args: Array<String>, var rawArgs: Array<String>, var strippedArgs: Array<String>) {
	var metadata = HashMap<String, Any>()
	var locale = loritta.getLocaleById("default")

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

	init {
		this.locale = locale
	}

	fun explain() {
		cmd.explain(this)
	}

	/**
	 * Verifica se o usuário tem permissão para utilizar um comando
	 */
	fun canUseCommand(): Boolean {
		return lorittaUser.canUseCommand(this)
	}

	fun getAsMention(addSpace: Boolean): String {
		val cmdOptions = lorittaUser.config.getCommandOptionsFor(cmd)
		return if (cmdOptions.override) {
			if (cmdOptions.mentionOnCommandOutput)
				lorittaUser.user.asMention + (if (addSpace) " " else "")
			else
				""
		} else lorittaUser.getAsMention(true)
	}

	@JvmOverloads
	fun reply(message: String, prefix: String? = null, forceMention: Boolean = false): Message {
		var send = ""
		if (prefix != null) {
			send = "$prefix **|** "
		}
		send = send + (if (forceMention) userHandle.asMention + " " else getAsMention(true)) + message
		return sendMessage(send)
	}

	fun reply(vararg loriReplies: LoriReply): Message {
		return reply(false, *loriReplies)
	}

	fun reply(mentionUserBeforeReplies: Boolean, vararg loriReplies: LoriReply): Message {
		val message = StringBuilder()
		if (mentionUserBeforeReplies && config.mentionOnCommandOutput) {
			message.append(LoriReply().build(this))
			message.append("\n")
		}
		for (loriReply in loriReplies) {
			message.append(loriReply.build(this))
			message.append("\n")
		}
		return sendMessage(message.toString())
	}

	fun reply(image: BufferedImage, fileName: String, vararg loriReplies: LoriReply): Message {
		val message = StringBuilder()
		for (loriReply in loriReplies) {
			message.append(loriReply.build(this) + "\n")
		}
		return sendFile(image, fileName, message.toString())
	}

	fun sendMessage(message: String): Message {
		return sendMessage(MessageBuilder().append(if (message.isEmpty()) " " else message).build())
	}

	fun sendMessage(message: Message): Message {
		var privateReply = lorittaUser.config.commandOutputInPrivate
		val cmdOptions = lorittaUser.config.getCommandOptionsFor(cmd)
		if (cmdOptions.override && cmdOptions.commandOutputInPrivate) {
			privateReply = cmdOptions.commandOutputInPrivate
		}
		if (privateReply || cmd is AjudaCommand) {
			return lorittaUser.user.openPrivateChannel().complete().sendMessage(message).complete()
		} else {
			if (isPrivateChannel || event.textChannel!!.canTalk()) {
				val sentMessage = event.channel.sendMessage(message).complete()
				return sentMessage
			} else {
				LorittaUtils.warnOwnerNoPermission(guild, event.textChannel, lorittaUser.config)
				throw RuntimeException("Sem permissão para enviar uma mensagem!")
			}
		}
	}

	fun sendMessage(message: String, embed: MessageEmbed): Message {
		return sendMessage(MessageBuilder().setEmbed(embed).append(if (message.isEmpty()) " " else message).build())
	}

	fun sendMessage(embed: MessageEmbed): Message {
		var privateReply = lorittaUser.config.commandOutputInPrivate
		val cmdOptions = lorittaUser.config.getCommandOptionsFor(cmd)
		if (cmdOptions.override && cmdOptions.commandOutputInPrivate) {
			privateReply = cmdOptions.commandOutputInPrivate
		}
		if (privateReply || cmd is AjudaCommand) {
			return lorittaUser.user.openPrivateChannel().complete().sendMessage(embed).complete()
		} else {
			if (isPrivateChannel || event.textChannel!!.canTalk()) {
				val sentMessage = event.channel.sendMessage(embed).complete()
				return sentMessage
			} else {
				LorittaUtils.warnOwnerNoPermission(guild, event.textChannel, lorittaUser.config)
				throw RuntimeException("Sem permissão para enviar uma mensagem!")
			}
		}
	}

	fun sendMessage(webhook: TemmieWebhook?, message: DiscordMessage) {
		if (!isPrivateChannel && webhook != null) { // Se a webhook é diferente de null, então use a nossa webhook disponível!
			webhook.sendMessage(message)
		} else { // Se não, iremos usar embeds mesmo...
			val builder = EmbedBuilder()
			builder.setAuthor(message.username, null, message.avatarUrl)
			builder.setDescription(message.content)
			builder.setFooter("Não consigo usar as permissões de webhook aqui... então estou usando o modo de pobre!", null)

			for (embed in message.embeds) {
				builder.setImage(if (embed.image != null) embed.image.url else null)
				if (embed.title != null) {
					builder.setTitle(builder.descriptionBuilder.toString() + "\n\n**" + embed.title + "**")
				}
				if (embed.description != null) {
					builder.setDescription(builder.descriptionBuilder.toString() + "\n\n" + embed.description)
				}
				if (embed.thumbnail != null) {
					builder.setThumbnail(embed.thumbnail.url)
				}
			}
			sendMessage(builder.build())
		}
	}

	fun sendFile(file: File, name: String, message: String, embed: MessageEmbed? = null): Message {
		// Corrigir erro ao construir uma mensagem vazia
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		if (embed != null)
			builder.setEmbed(embed)
		return sendFile(file, name, builder.build())
	}

	fun sendFile(file: File, name: String, message: Message): Message {
		val inputStream = file.inputStream()
		return sendFile(inputStream, name, message)
	}

	fun sendFile(image: BufferedImage, name: String, embed: MessageEmbed): Message {
		return sendFile(image, name, "", embed)
	}

	fun sendFile(image: BufferedImage, name: String, message: String, embed: MessageEmbed? = null): Message {
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		if (embed != null)
			builder.setEmbed(embed)

		return sendFile(image, name, builder.build())
	}

	fun sendFile(image: BufferedImage, name: String, message: Message): Message {
		val outputStream = ByteArrayOutputStream()
		outputStream.use {
			ImageIO.write(image, "png", it)
		}

		val inputStream = ByteArrayInputStream(outputStream.toByteArray())

		return sendFile(inputStream, name, message)
	}

	fun sendFile(inputStream: InputStream, name: String, message: String): Message {
		// Corrigir erro ao construir uma mensagem vazia
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		return sendFile(inputStream, name, builder.build())
	}

	fun sendFile(inputStream: InputStream, name: String, embed: MessageEmbed): Message {
		return sendFile(inputStream, name, "", embed)
	}

	fun sendFile(inputStream: InputStream, name: String, message: String, embed: MessageEmbed? = null): Message {
		// Corrigir erro ao construir uma mensagem vazia
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		if (embed != null)
			builder.setEmbed(embed)
		return sendFile(inputStream, name, builder.build())
	}

	fun sendFile(inputStream: InputStream, name: String, message: Message): Message {
		var privateReply = lorittaUser.config.commandOutputInPrivate
		val cmdOptions = lorittaUser.config.getCommandOptionsFor(cmd)
		if (cmdOptions.override && cmdOptions.commandOutputInPrivate) {
			privateReply = cmdOptions.commandOutputInPrivate
		}
		if (privateReply || cmd is AjudaCommand) {
			val message = lorittaUser.user.openPrivateChannel().complete().sendFile(inputStream, name, message).complete()
			inputStream.close()
			return message
		} else {
			if (isPrivateChannel || event.textChannel!!.canTalk()) {
				val sentMessage = event.channel.sendFile(inputStream, name, message).complete()
				inputStream.close()
				return sentMessage
			} else {
				LorittaUtils.warnOwnerNoPermission(guild, event.textChannel, lorittaUser.config)
				throw RuntimeException("Sem permissão para enviar uma mensagem!")
			}
		}
	}

	/**
	 * Gets an user from the argument index via mentions, username#discriminator, effective name, username and user ID
	 *
	 * @param argument the argument index on the rawArgs array
	 * @return         the user object or null, if nothing was found
	 * @see            User
	 */
	fun getUserAt(argument: Int): User? {
		if (this.rawArgs.size > argument) { // Primeiro iremos verificar se existe uma imagem no argumento especificado
			val link = this.rawArgs[argument] // Ok, será que isto é uma URL?

			// Vamos verificar por menções, uma menção do Discord é + ou - assim: <@123170274651668480>
			for (user in this.message.mentionedUsers) {
				if (user.asMention == link.replace("!", "")) { // O replace é necessário já que usuários com nick tem ! no mention (?)
					// Diferente de null? Então vamos usar o avatar do usuário!
					return user
				}
			}

			// Vamos tentar procurar pelo username + discriminator
			if (!this.isPrivateChannel && !link.isEmpty()) {
				val split = link.split("#").dropLastWhile { it.isEmpty() }.toTypedArray()

				if (split.size == 2 && split[0].isNotEmpty()) {
					val matchedMember = this.guild.getMembersByName(split[0], false).stream().filter { it -> it.user.discriminator == split[1] }.findFirst()

					if (matchedMember.isPresent) {
						return matchedMember.get().user
					}
				}
			}

			// Ok então... se não é link e nem menção... Que tal então verificar por nome?
			if (!this.isPrivateChannel && !link.isEmpty()) {
				val matchedMembers = this.guild.getMembersByEffectiveName(link, true)

				if (!matchedMembers.isEmpty()) {
					return matchedMembers[0].user
				}
			}

			// Se não, vamos procurar só pelo username mesmo
			if (!this.isPrivateChannel && !link.isEmpty()) {
				val matchedMembers = this.guild.getMembersByName(link, true)

				if (!matchedMembers.isEmpty()) {
					return matchedMembers[0].user
				}
			}

			// Ok, então só pode ser um ID do Discord!
			try {
				val user = LorittaLauncher.loritta.lorittaShards.retrieveUserById(link)

				if (user != null) { // Pelo visto é!
					return user
				}
			} catch (e: Exception) {
			}
		}
		return null
	}

	/**
	 * Gets an image URL from the argument index via valid URLs at the specified index
	 *
	 * @param argument   the argument index on the rawArgs array
	 * @param search     how many messages will be retrieved from the past to get images (default: 25)
	 * @param avatarSize the size of retrieved user avatars from Discord (default: 2048)
	 * @return           the image URL or null, if nothing was found
	 */
	fun getImageUrlAt(argument: Int, search: Int = 25, avatarSize: Int = 2048): String? {
		if (this.rawArgs.size > argument) { // Primeiro iremos verificar se existe uma imagem no argumento especificado
			val link = this.rawArgs[argument] // Ok, será que isto é uma URL?

			if (LorittaUtils.isValidUrl(link))
				return link // Se é um link, vamos enviar para o usuário agora

			// Vamos verificar por usuários no argumento especificado
			val user = getUserAt(argument)
			if (user != null)
				return user.effectiveAvatarUrl + "?size=" + avatarSize

			// Ainda não?!? Vamos verificar se é um emoji.
			// Um emoji custom do Discord é + ou - assim: <:loritta:324931508542504973>
			for (emote in this.message.emotes) {
				if (link.equals(emote.asMention, ignoreCase = true)) {
					return emote.imageUrl
				}
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

		// Ainda nada válido? Quer saber, desisto! Vamos pesquisar as mensagens antigas deste servidor & embeds então para encontrar attachments...
		if (search > 0 && !this.isPrivateChannel && this.guild.selfMember.hasPermission(this.event.textChannel, Permission.MESSAGE_HISTORY)) {
			try {
				val message = this.message.channel.history.retrievePast(search).complete()

				attach@ for (msg in message) {
					for (embed in msg.embeds) {
						if (embed.image != null) {
							return embed.image.url
						}
					}
					for (attachment in msg.attachments) {
						if (attachment.isImage) {
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
	fun getImageAt(argument: Int, search: Int = 25, avatarSize: Int = 2048): BufferedImage? {
		var toBeDownloaded = getImageUrlAt(argument, search, avatarSize) ?: return null

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
			return LorittaUtils.downloadImage(toBeDownloaded)
		} catch (e: Exception) {
			return null
		}
	}
}
