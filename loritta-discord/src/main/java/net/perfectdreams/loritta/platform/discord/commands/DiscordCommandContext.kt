package net.perfectdreams.loritta.platform.discord.commands

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.PermissionException
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.entities.User
import net.perfectdreams.loritta.api.utils.image.Image
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.entities.DiscordMessage
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser
import net.perfectdreams.loritta.utils.Emotes
import org.jsoup.Jsoup
import java.io.File
import java.io.InputStream

class DiscordCommandContext(
		loritta: LorittaDiscord,
		command: Command<CommandContext>,
		args: List<String>,
		val discordMessage: Message,
		locale: BaseLocale,
		val serverConfig: ServerConfig,
		val lorittaUser: LorittaUser
) : CommandContext(loritta, command, args, DiscordMessage(discordMessage), locale) {
	val isPrivateChannel = discordMessage.channelType == ChannelType.PRIVATE
	val guild: Guild
		get() = discordMessage.guild
	val user = discordMessage.author
	val member = discordMessage.member

	suspend fun sendMessage(message: String, embed: MessageEmbed): Message {
		return sendMessage(MessageBuilder().setEmbed(embed).append(if (message.isEmpty()) " " else message).build())
	}

	suspend fun sendMessage(embed: MessageEmbed): Message {
		return sendMessage(MessageBuilder().append(getUserMention(true)).setEmbed(embed).build())
	}

	suspend fun sendMessage(message: Message): Message {
		if (isPrivateChannel || discordMessage.textChannel.canTalk()) {
			val sentMessage = discordMessage.channel.sendMessage(message).await()
			return sentMessage
		} else {
			// LorittaUtils.warnOwnerNoPermission(discordGuild, event.textChannel, config)
			throw RuntimeException("Sem permissão para enviar uma mensagem!")
		}
	}

	suspend fun sendFile(file: File, fileName: String, content: String = this.getUserMention(true), embed: MessageEmbed? = null): DiscordMessage {
		return DiscordMessage(discordMessage.channel.sendMessage(
				MessageBuilder()
						.append(content)
						.setEmbed(embed)
						.build()
		)
				.addFile(file, fileName)
				.await()
		)
	}

	suspend fun sendFile(inputStream: InputStream, fileName: String, content: String = this.getUserMention(true), embed: MessageEmbed? = null): DiscordMessage {
		return DiscordMessage(discordMessage.channel.sendMessage(
				MessageBuilder()
						.append(content)
						.setEmbed(embed)
						.build()
		)
				.addFile(inputStream, fileName)
				.await()
		)
	}

	override suspend fun user(argument: Int): User? {
		if (this.args.size > argument) { // Primeiro iremos verificar se existe uma imagem no argumento especificado
			val link = this.args[argument] // Ok, será que isto é uma URL?

			// Vamos verificar por menções, uma menção do Discord é + ou - assim: <@123170274651668480>
			for (user in this.message.mentionedUsers) {
				if (user.asMention == link.replace("!", "")) { // O replace é necessário já que usuários com nick tem ! no mention (?)
					// Diferente de null? Então vamos usar o avatar do usuário!
					return user
				}
			}

			// Ok, então só pode ser um ID do Discord!
			try {
				val user = LorittaLauncher.loritta.lorittaShards.retrieveUserById(link)

				if (user != null) // Pelo visto é!
					return JDAUser(user)
			} catch (e: Exception) {
			}
		}
		return null
	}

	override suspend fun imageUrl(argument: Int, searchPreviousMessages: Int): String? {
		if (this.args.size > argument) { // Primeiro iremos verificar se existe uma imagem no argumento especificado
			val link = this.args[argument] // Ok, será que isto é uma URL?

			if (LorittaUtils.isValidUrl(link))
				return link // Se é um link, vamos enviar para o usuário agora

			// Vamos verificar por usuários no argumento especificado
			val user = user(argument)
			if (user != null)
				return user.avatarUrl + "?size=256"

			// Ainda não?!? Vamos verificar se é um emoji.
			// Um emoji custom do Discord é + ou - assim: <:loritta:324931508542504973>
			for (emote in this.discordMessage.emotes) {
				if (link.equals(emote.asMention, ignoreCase = true)) {
					return emote.imageUrl
				}
			}

			// Se não é nada... então talvez seja um emoji padrão do Discordão!
			// Na verdade é um emoji padrão...
			try {
				var unicodeEmoji = LorittaUtils.toUnicode(this.args[argument].codePointAt(0)) // Vamos usar codepoints porque emojis
				unicodeEmoji = unicodeEmoji.substring(2) // Remover coisas desnecessárias
				val toBeDownloaded = "https://twemoji.maxcdn.com/2/72x72/$unicodeEmoji.png"
				if (HttpRequest.get(toBeDownloaded).code() == 200) {
					return toBeDownloaded
				}
			} catch (e: Exception) {
			}
		}

		// Ainda nada válido? Quer saber, desisto! Vamos pesquisar as mensagens antigas deste servidor & embeds então para encontrar attachments...
		if (searchPreviousMessages > 0 && !this.isPrivateChannel && guild.selfMember.hasPermission(message.channel as TextChannel, Permission.MESSAGE_HISTORY)) {
			val textChannel = message.channel as TextChannel
			try {
				val message = textChannel.history.retrievePast(searchPreviousMessages).await()

				attach@ for (msg in message) {
					for (embed in msg.embeds) {
						if (embed.image != null) {
							return embed.image!!.url
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

	override suspend fun image(argument: Int, searchPreviousMessages: Int, createTextAsImageIfNotFound: Boolean): Image? {
		var toBeDownloaded = imageUrl(argument)

		if (toBeDownloaded == null) {
			if (args.isNotEmpty() && createTextAsImageIfNotFound) {
				val theTextThatWillBeWritten = args.drop(argument).joinToString(" ")
				if (theTextThatWillBeWritten.isNotEmpty())
					return JVMImage(ImageUtils.createTextAsImage(256, 256, theTextThatWillBeWritten))
			}

			if (searchPreviousMessages != 0) {
				toBeDownloaded = imageUrl(argument, searchPreviousMessages)
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
			val image = LorittaUtils.downloadImage(toBeDownloaded ?: return null) ?: return null
			return JVMImage(image)
		} catch (e: Exception) {
			return null
		}
	}

	/**
	 * Sends an embed explaining what the command does
	 *
	 * @param context the context of the command
	 */
	override suspend fun explain() {
		val embed = EmbedBuilder()
				.setColor(Constants.LORITTA_AQUA)
				.setAuthor(user.name + "#" + user.discriminator, null, user.effectiveAvatarUrl)
				.setTitle("${Emotes.LORI_HM} `${serverConfig.commandPrefix}${command.labels.first()}`")
				.setDescription(command.description.invoke(locale))

		val messageBuilder = MessageBuilder()
				.append(getUserMention(true))
				.setEmbed(embed.build())

		discordMessage.channel.sendMessage(messageBuilder.build()).await()
	}
}