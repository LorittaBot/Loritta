package com.mrpowergamerbr.loritta.commands

import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.vanilla.misc.AjudaCommand
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.*
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
class CommandContext(val config: ServerConfig, var lorittaUser: LorittaUser, locale: BaseLocale, var event: AbstractCommand.LorittaMessageEvent, var cmd: AbstractCommand, var args: Array<String>, var rawArgs: Array<String>, var strippedArgs: Array<String>) {
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
		val message = StringBuilder()
		for (loriReply in loriReplies) {
			message.append(loriReply.build(this) + "\n")
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
				LorittaLauncher.loritta.messageContextCache.put(sentMessage.id, this)
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
				LorittaLauncher.loritta.messageContextCache.put(sentMessage.id, this)
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
				LorittaLauncher.loritta.messageContextCache[sentMessage.id] = this
				inputStream.close()
				return sentMessage
			} else {
				LorittaUtils.warnOwnerNoPermission(guild, event.textChannel, lorittaUser.config)
				throw RuntimeException("Sem permissão para enviar uma mensagem!")
			}
		}
	}
}
