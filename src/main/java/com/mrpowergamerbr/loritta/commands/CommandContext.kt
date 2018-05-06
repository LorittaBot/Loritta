package com.mrpowergamerbr.loritta.commands

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.commands.vanilla.misc.AjudaCommand
import com.mrpowergamerbr.loritta.userdata.ServerConfig
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.temmiewebhook.DiscordEmbed
import com.mrpowergamerbr.temmiewebhook.DiscordMessage
import com.mrpowergamerbr.temmiewebhook.TemmieWebhook
import net.dv8tion.jda.core.EmbedBuilder
import net.dv8tion.jda.core.MessageBuilder
import net.dv8tion.jda.core.entities.ChannelType
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.Member
import net.dv8tion.jda.core.entities.Message
import net.dv8tion.jda.core.entities.MessageEmbed
import net.dv8tion.jda.core.entities.User
import net.dv8tion.jda.core.events.message.MessageReceivedEvent

import javax.imageio.ImageIO
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.util.HashMap

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
			send = prefix + " **|** "
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

	fun sendFile(image: BufferedImage, name: String, message: String): Message {
		val os = ByteArrayOutputStream()
		try {
			ImageIO.write(image, "png", os)
		} catch (e: Exception) {
		}

		val `is` = ByteArrayInputStream(os.toByteArray())

		val discordMessage = sendFile(`is`, name, message)
		try {
			os.close()
			`is`.close()
		} catch (e: IOException) {
			e.printStackTrace()
		}

		return discordMessage
	}

	fun sendFile(image: BufferedImage, name: String, message: Message): Message {
		val os = ByteArrayOutputStream()
		try {
			ImageIO.write(image, "png", os)
		} catch (e: Exception) {
		}

		val `is` = ByteArrayInputStream(os.toByteArray())

		val discordMessage = sendFile(`is`, name, message)
		try {
			os.close()
			`is`.close()
		} catch (e: IOException) {
			e.printStackTrace()
		}

		return discordMessage
	}

	fun sendFile(image: BufferedImage, name: String, message: MessageEmbed): Message {
		val os = ByteArrayOutputStream()
		try {
			ImageIO.write(image, "png", os)
		} catch (e: Exception) {
		}

		val `is` = ByteArrayInputStream(os.toByteArray())

		val discordMessage = sendFile(`is`, name, message)
		try {
			os.close()
			`is`.close()
		} catch (e: IOException) {
			e.printStackTrace()
		}

		return discordMessage
	}

	fun sendFile(data: InputStream, name: String, message: String): Message {
		// Corrigir erro ao construir uma mensagem vazia
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		return sendFile(data, name, builder.build())
	}

	fun sendFile(data: InputStream, name: String, message: MessageEmbed): Message {
		val messageBuilder = MessageBuilder()
		messageBuilder.setEmbed(message)
		messageBuilder.append(" ")
		return sendFile(data, name, messageBuilder.build())
	}

	fun sendFile(data: InputStream, name: String, message: Message): Message {
		var privateReply = lorittaUser.config.commandOutputInPrivate
		val cmdOptions = lorittaUser.config.getCommandOptionsFor(cmd)
		if (cmdOptions.override && cmdOptions.commandOutputInPrivate) {
			privateReply = cmdOptions.commandOutputInPrivate
		}
		if (privateReply || cmd is AjudaCommand) {
			return lorittaUser.user.openPrivateChannel().complete().sendFile(data, name, message).complete()
		} else {
			if (isPrivateChannel || event.textChannel!!.canTalk()) {
				val sentMessage = event.channel.sendFile(data, name, message).complete()
				LorittaLauncher.loritta.messageContextCache.put(sentMessage.id, this)
				return sentMessage
			} else {
				LorittaUtils.warnOwnerNoPermission(guild, event.textChannel, lorittaUser.config)
				throw RuntimeException("Sem permissão para enviar uma mensagem!")
			}
		}
	}

	@Throws(IOException::class)
	fun sendFile(file: File, name: String, message: String): Message {
		// Corrigir erro ao construir uma mensagem vazia
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		return sendFile(file, name, builder.build())
	}

	@Throws(IOException::class)
	fun sendFile(file: File, name: String, message: Message): Message {
		var privateReply = lorittaUser.config.commandOutputInPrivate
		val cmdOptions = lorittaUser.config.getCommandOptionsFor(cmd)
		if (cmdOptions.override && cmdOptions.commandOutputInPrivate) {
			privateReply = cmdOptions.commandOutputInPrivate
		}
		if (privateReply || cmd is AjudaCommand) {
			return lorittaUser.user.openPrivateChannel().complete().sendFile(file, name, message).complete()
		} else {
			if (isPrivateChannel || event.textChannel!!.canTalk()) {
				val sentMessage = event.channel.sendFile(file, name, message).complete()
				LorittaLauncher.loritta.messageContextCache.put(sentMessage.id, this)
				return sentMessage
			} else {
				LorittaUtils.warnOwnerNoPermission(guild, event.textChannel, lorittaUser.config)
				throw RuntimeException("Sem permissão para enviar uma mensagem!")
			}
		}
	}
}
