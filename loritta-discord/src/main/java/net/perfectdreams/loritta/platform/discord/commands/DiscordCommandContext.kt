package net.perfectdreams.loritta.platform.discord.commands

import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.utils.Constants
import com.mrpowergamerbr.loritta.utils.ImageUtils
import com.mrpowergamerbr.loritta.utils.LorittaUser
import com.mrpowergamerbr.loritta.utils.LorittaUtils
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.localized
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.PermissionException
import net.perfectdreams.loritta.api.commands.Command
import net.perfectdreams.loritta.api.commands.CommandContext
import net.perfectdreams.loritta.api.messages.LorittaMessage
import net.perfectdreams.loritta.api.utils.image.Image
import net.perfectdreams.loritta.api.utils.image.JVMImage
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.platform.discord.entities.DiscordMessage
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser
import net.perfectdreams.loritta.utils.DiscordUtils
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.ImageFormat
import org.jsoup.Jsoup
import java.io.File
import java.io.InputStream
import java.time.Instant

class DiscordCommandContext(
		override val loritta: LorittaDiscord,
		command: Command<CommandContext>,
		args: List<String>,
		val discordMessage: Message,
		locale: BaseLocale,
		val serverConfig: ServerConfig,
		val lorittaUser: LorittaUser,
		val executedCommandLabel: String
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
			return discordMessage.channel.sendMessage(message)
					.reference(discordMessage)
					.await()
		} else {
			throw RuntimeException("Sem permissão para enviar uma mensagem!")
		}
	}

	override suspend fun sendImage(image: Image, fileName: String, content: String): net.perfectdreams.loritta.api.entities.Message {
		return DiscordMessage(
				discordMessage.channel.sendMessage(LorittaMessage(content).content)
						.addFile(image.toByteArray(), fileName)
						.reference(discordMessage)
						.await()
		)
	}

	override suspend fun sendFile(byteArray: ByteArray, fileName: String, content: String): net.perfectdreams.loritta.api.entities.Message {
		return DiscordMessage(
				discordMessage.channel.sendMessage(LorittaMessage(content).content)
						.addFile(byteArray, fileName)
						.reference(discordMessage)
						.await()
		)
	}

	suspend fun sendFile(file: File, fileName: String, content: String = this.getUserMention(true), embed: MessageEmbed? = null): DiscordMessage {
		return DiscordMessage(discordMessage.channel.sendMessage(
				MessageBuilder()
						.append(content)
						.setEmbed(embed)
						.build()
		)
				.addFile(file, fileName)
				.reference(discordMessage)
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
				.reference(discordMessage)
				.await()
		)
	}

	override suspend fun user(argument: Int) = this.args.getOrNull(argument)
			?.let {
				DiscordUtils.extractUserFromString(
						it,
						discordMessage.mentionedUsers,
						if (isPrivateChannel) null else discordMessage.guild
				)?.let { JDAUser(it) }
			}

	override suspend fun imageUrl(argument: Int, searchPreviousMessages: Int): String? {
		if (this.args.size > argument) { // Primeiro iremos verificar se existe uma imagem no argumento especificado
			val link = this.args[argument] // Ok, será que isto é uma URL?

			if (LorittaUtils.isValidUrl(link)) {
				// Workaround for direct prnt.sc image links (Lightshot is trash but a lot of people use it)
				if (link.contains("prnt.sc")) {
					val document = withContext(Dispatchers.IO) { Jsoup.connect(link).get() }
					val elements = document.getElementsByAttributeValue("property", "og:image")
					if (!elements.isEmpty()) {
						return elements.attr("content")
					}
				}

				return link // Se é um link, vamos enviar para o usuário agora
			}

			// Vamos verificar por usuários no argumento especificado
			val user = user(argument)
			if (user != null)
				return user.getEffectiveAvatarUrl(ImageFormat.PNG, 256)

			// Ainda não?!? Vamos verificar se é um emoji.
			// Um emoji custom do Discord é + ou - assim: <:loritta:324931508542504973>
			for (emote in this.discordMessage.emotes) {
				if (link.equals(emote.asMention, ignoreCase = true)) {
					return emote.imageUrl
				}
			}

			for (embed in discordMessage.embeds) {
				if (embed.image != null)
					return embed.image!!.url
			}
			for (attachment in discordMessage.attachments) {
				if (attachment.isImage)
					return attachment.url
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

		// Nothing found? Try retrieving the replied message content
		val referencedMessage = discordMessage.referencedMessage
		if (referencedMessage != null) {
			for (embed in referencedMessage.embeds) {
				if (embed.image != null)
					return embed.image!!.url
			}
			for (attachment in referencedMessage.attachments) {
				if (attachment.isImage)
					return attachment.url
			}
		}

		// Ainda nada válido? Quer saber, desisto! Vamos pesquisar as mensagens antigas deste servidor & embeds então para encontrar attachments...
		if (searchPreviousMessages > 0 && !this.isPrivateChannel && guild.selfMember.hasPermission(discordMessage.channel as TextChannel, Permission.MESSAGE_HISTORY)) {
			val textChannel = discordMessage.channel as TextChannel
			try {
				val message = textChannel.history.retrievePast(searchPreviousMessages).await()

				attach@ for (msg in message) {
					for (embed in msg.embeds) {
						if (embed.image != null)
							return embed.image!!.url
					}
					for (attachment in msg.attachments) {
						if (attachment.isImage)
							return attachment.url
					}
				}
			} catch (e: PermissionException) {
			}
		}

		return null
	}

	override suspend fun image(argument: Int, searchPreviousMessages: Int, createTextAsImageIfNotFound: Boolean): Image? {
		var toBeDownloaded = imageUrl(argument, 0)

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
			val image = LorittaUtils.downloadImage(toBeDownloaded) ?: return null
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
				.setAuthor(locale["commands.explain.clickHereToSeeAllMyCommands"], "${loritta.instanceConfig.loritta.website.url}commands", discordMessage.jda.selfUser.effectiveAvatarUrl)
				.setTitle("${Emotes.LORI_HM} `${serverConfig.commandPrefix}${executedCommandLabel}`")
				.setFooter("${user.name + "#" + user.discriminator} • ${command.category.getLocalizedName(locale)}", user.effectiveAvatarUrl)
				.setTimestamp(Instant.now())

		val description = buildString {
			this.append(command.description.invoke(locale))
			this.append('\n')
			this.append('\n')
			this.append("${Emotes.LORI_SMILE} **${locale["commands.explain.howToUse"]}** ")
			this.append('`')
			this.append(serverConfig.commandPrefix)
			this.append(command.labels.first())
			this.append('`')
			this.append(' ')
			for ((index, argument) in command.usage.arguments.withIndex()) {
				// <argumento> - Argumento obrigatório
				// [argumento] - Argumento opcional
				this.append("**")
				this.append('`')
				argument.build(this, locale)
				this.append('`')
				this.append("**")
				if (index != command.usage.arguments.size - 1)
					this.append(' ')
			}
		}

		embed.setDescription(description)
		val examples = command.examples?.invoke(locale)

		if (examples != null) {
			embed.addField(
					"\uD83D\uDCD6 ${locale["commands.explain.examples"]}",
					examples.joinToString("\n", transform = { "`${serverConfig.commandPrefix}${executedCommandLabel}` **`${it}`**" }),
					false
			)
		}

		if (command is DiscordCommand) {
			if (command.botRequiredPermissions.isNotEmpty() || command.userRequiredPermissions.isNotEmpty()) {
				var field = ""
				if (command.userRequiredPermissions.isNotEmpty()) {
					field += "\uD83D\uDC81 ${locale["commands.explain.youNeedToHavePermission", command.userRequiredPermissions.joinToString(", ", transform = { "`${it.localized(locale)}`" })]}\n"
				}
				if (command.botRequiredPermissions.isNotEmpty()) {
					field += "<:loritta:331179879582269451> ${locale["commands.explain.loriNeedToHavePermission", command.botRequiredPermissions.joinToString(", ", transform = { "`${it.localized(locale)}`" })]}\n"
				}
				embed.addField(
						"\uD83D\uDCDB ${locale["commands.explain.permissions"]}",
						field,
						false
				)
			}
		}

		val otherAlternatives = command.labels.filter { it != executedCommandLabel }

		if (otherAlternatives.isNotEmpty()) {
			embed.addField(
					"\uD83D\uDD00 ${locale["commands.explain.aliases"]}",
					otherAlternatives.joinToString(transform = { "`${serverConfig.commandPrefix}$it`" }),
					false
			)
		}

		val similarCommands = loritta.commandMap.commands.filter {
			it.commandName != command.commandName && it.commandName in command.similarCommands
		}

		if (similarCommands.isNotEmpty()) {
			embed.addField(
					"${Emotes.LORI_WOW} ${locale["commands.explain.relatedCommands"]}",
					similarCommands.joinToString(transform = { "`${serverConfig.commandPrefix}${it.labels.first()}`" }),
					false
			)
		}

		val messageBuilder = MessageBuilder()
				.append(getUserMention(true))
				.setEmbed(embed.build())

		discordMessage.channel.sendMessage(messageBuilder.build())
				.reference(discordMessage)
				.await()
	}
}