package net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands

import com.github.kevinsawicki.http.HttpRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji
import net.dv8tion.jda.api.exceptions.PermissionException
import net.dv8tion.jda.api.utils.FileUpload
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.locale.LocaleStringData
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.common.utils.image.Image
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.api.commands.Command
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.messages.LorittaMessage
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.DiscordMessage
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.jda.JDAUser
import net.perfectdreams.loritta.morenitta.utils.*
import net.perfectdreams.loritta.morenitta.utils.extensions.*
import org.jsoup.Jsoup
import java.io.File
import java.io.InputStream
import java.time.Instant

class DiscordCommandContext(
	loritta: LorittaBot,
	command: Command<CommandContext>,
	args: List<String>,
	val discordMessage: Message,
	locale: BaseLocale,
	i18nContext: I18nContext,
	val serverConfig: ServerConfig,
	val lorittaUser: LorittaUser,
	val executedCommandLabel: String
) : CommandContext(loritta, command, args, DiscordMessage(discordMessage), locale, i18nContext) {
	val isPrivateChannel = discordMessage.channelType == ChannelType.PRIVATE
	val guild: Guild
		get() = discordMessage.guild
	val user = discordMessage.author
	val member = discordMessage.member

	suspend fun sendMessage(message: String, embed: MessageEmbed): Message {
		return sendMessage(MessageCreateBuilder()
			.denyMentions(
				Message.MentionType.EVERYONE,
				Message.MentionType.HERE
			)
			.setEmbeds(embed)
			.addContent(if (message.isEmpty()) " " else message)
			.build()
		)
	}

	suspend fun sendMessageEmbeds(embed: MessageEmbed): Message {
		return sendMessage(MessageCreateBuilder()
			.denyMentions(
				Message.MentionType.EVERYONE,
				Message.MentionType.HERE
			)
			.addContent(getUserMention(true))
			.setEmbeds(embed)
			.build()
		)
	}

	suspend fun sendMessage(message: MessageCreateData): Message {
		if (isPrivateChannel || discordMessage.guildChannel.canTalk()) {
			return discordMessage.channel.sendMessage(message)
				.referenceIfPossible(discordMessage, serverConfig, true)
				.await()
		} else {
			throw RuntimeException("Sem permissão para enviar uma mensagem!")
		}
	}

	override suspend fun sendImage(image: Image, fileName: String, content: String): net.perfectdreams.loritta.morenitta.api.entities.Message {
		return DiscordMessage(
			discordMessage.channel.sendMessage(LorittaMessage(content).content)
				.addFiles(FileUpload.fromData(image.toByteArray(), fileName))
				.referenceIfPossible(discordMessage, serverConfig, true)
				.await()
		)
	}

	override suspend fun sendFile(byteArray: ByteArray, fileName: String, content: String): net.perfectdreams.loritta.morenitta.api.entities.Message {
		return DiscordMessage(
			discordMessage.channel.sendMessage(LorittaMessage(content).content)
				.addFiles(FileUpload.fromData(byteArray, fileName))
				.referenceIfPossible(discordMessage, serverConfig, true)
				.await()
		)
	}

	suspend fun sendFile(file: File, fileName: String, content: String = this.getUserMention(true), embed: MessageEmbed? = null): DiscordMessage {
		return DiscordMessage(
			discordMessage.channel.sendMessage(
				MessageCreateBuilder()
					.denyMentions(
						Message.MentionType.EVERYONE,
						Message.MentionType.HERE
					)
					.addContent(content)
					.apply {
						if (embed != null)
							setEmbeds(embed)
					}
					.build()
			)
				.addFiles(FileUpload.fromData(file, fileName))
				.referenceIfPossible(discordMessage, serverConfig, true)
				.await()
		)
	}

	suspend fun sendFile(inputStream: InputStream, fileName: String, content: String = this.getUserMention(true), embed: MessageEmbed? = null): DiscordMessage {
		return DiscordMessage(discordMessage.channel.sendMessage(
			MessageCreateBuilder()
				.denyMentions(
					Message.MentionType.EVERYONE,
					Message.MentionType.HERE
				)
				.addContent(content)
				.apply {
					if (embed != null)
						setEmbeds(embed)
				}
				.build()
		)
			.addFiles(FileUpload.fromData(inputStream, fileName))
			.referenceIfPossible(discordMessage, serverConfig, true)
			.await()
		)
	}

	override suspend fun user(argument: Int) = this.args.getOrNull(argument)
		?.let {
			DiscordUtils.extractUserFromString(
				loritta,
				it,
				discordMessage.mentions.users,
				if (isPrivateChannel) null else discordMessage.guild
			)?.let { JDAUser(it) }
		}

	override suspend fun imageUrl(argument: Int, searchPreviousMessages: Int): String? {
		if (this.args.size > argument) { // Primeiro iremos verificar se existe uma imagem no argumento especificado
			val link = this.args[argument] // Ok, será que isto é uma URL?

			if (LorittaUtils.isValidUrl(link) && loritta.connectionManager.isTrusted(link)) {
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
			for (emote in this.discordMessage.mentions.customEmojis) {
				if (link.equals(emote.asMention, ignoreCase = true)) {
					return emote.imageUrl
				}
			}

			for (embed in discordMessage.embeds) {
				if (embed.image != null && loritta.connectionManager.isTrusted(embed.image!!.url!!))
					return embed.image!!.url
			}
			for (attachment in discordMessage.attachments) {
				if (attachment.isImage && loritta.connectionManager.isTrusted(attachment.url))
					return attachment.url
			}

			// Se não é nada... então talvez seja um emoji padrão do Discordão!
			// Na verdade é um emoji padrão...
			try {
				var unicodeEmoji = LorittaUtils.toUnicode(this.args[argument].codePointAt(0)) // Vamos usar codepoints porque emojis
				unicodeEmoji = unicodeEmoji.substring(2) // Remover coisas desnecessárias
				val toBeDownloaded = "https://abs.twimg.com/emoji/v2/72x72/$unicodeEmoji.png"
				if (HttpRequest.get(toBeDownloaded).code() == 200) {
					return toBeDownloaded
				}
			} catch (e: Exception) {
			}
		}

		// Nothing found? Try retrieving the replied message content
		if (!this.isPrivateChannel && this.guild.selfMember.hasPermission(this.discordMessage.guildChannel, Permission.MESSAGE_HISTORY)) {
			val referencedMessage = discordMessage.referencedMessage
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

		// Still nothing valid? You know what? I give up! Let's search old messages from this server & embeds to find attachments...
		if (searchPreviousMessages > 0 && !this.isPrivateChannel && guild.selfMember.hasPermission(discordMessage.channel.asGuildMessageChannel(), Permission.MESSAGE_HISTORY)) {
			val messageChannel = discordMessage.channel.asGuildMessageChannel()
			try {
				val message = messageChannel.history.retrievePast(searchPreviousMessages).await()

				attach@ for (msg in message) {
					for (embed in msg.embeds) {
						if (embed.image != null && loritta.connectionManager.isTrusted(embed.image!!.url!!))
							return embed.image!!.url
					}
					for (attachment in msg.attachments) {
						if (attachment.isImage && loritta.connectionManager.isTrusted(attachment.url))
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
					return JVMImage(ImageUtils.createTextAsImage(loritta, 256, 256, theTextThatWillBeWritten))
			}

			if (searchPreviousMessages != 0) {
				toBeDownloaded = imageUrl(argument, searchPreviousMessages)
			}
		}

		if (toBeDownloaded == null)
			return null

		// let's download the image!
		try {
			val image = LorittaUtils.downloadImage(loritta, toBeDownloaded) ?: return null
			return JVMImage(image)
		} catch (e: Exception) {
			return null
		}
	}

	fun textChannel(argument: Int): TextChannel? {
		val channelId = args.getOrNull(argument)
			?.replace("<#", "")
			?.replace(">", "")

		return if (channelId?.isValidSnowflake()!!) {
			guild.getTextChannelById(channelId)
		} else {
			null
		} ?: if (guild.getTextChannelsByName(args[0], true).isNotEmpty()) {
			guild.getTextChannelsByName(args[0], true).first()
		} else {
			null
		} ?: if (guild.textChannels.filter { it.name == args[0] }.isNotEmpty()) {
			guild.textChannels.filter { it.name == args[0] }.first()
		} else {
			null
		}
	}

	fun voiceChannel(argument: Int): VoiceChannel? {
		val channelId = args.getOrNull(argument)
			?.replace("<#", "")
			?.replace(">", "")

		return if (channelId?.isValidSnowflake()!!) {
			guild.getVoiceChannelById(channelId)
		} else {
			null
		} ?: if (guild.getVoiceChannelsByName(args[0], true).isNotEmpty()) {
			guild.getVoiceChannelsByName(args[0], true).first()
		} else {
			null
		} ?: if (guild.voiceChannels.filter { it.name == args[0] }.isNotEmpty()) {
			guild.voiceChannels.filter { it.name == args[0] }.first()
		} else {
			null
		}
	}

	fun role(argument: Int): Role? {
		val roleId = args.getOrNull(argument)
			?.replace("<@&", "")
			?.replace(">", "")

		return if (roleId?.isValidSnowflake()!!) {
			guild.getRoleById(roleId)
		} else {
			null
		} ?: if (guild.getRolesByName(args[0], true).isNotEmpty()) {
			guild.getRolesByName(args[0], true).first()
		} else {
			null
		} ?: if (guild.roles.filter { it.name == args[0] }.isNotEmpty()) {
			guild.roles.filter { it.name == args[0] }.first()
		} else {
			null
		}
	}

	fun emote(argument: Int): RichCustomEmoji? {
		val regexEmote = Regex("(<)|[a-z]|(_)|(:)|(>)")
		val emoteId = args.getOrNull(argument)?.let { regexEmote.replace(it, "") }

		return if (emoteId?.isValidSnowflake()!!) {
			guild.getEmojiById(emoteId)
		} else {
			null
		} ?: if (guild.getEmojisByName(args[0], true).isNotEmpty()) {
			guild.getEmojisByName(args[0], true).first()
		} else {
			null
		} ?: if (guild.emojis.filter { it.name == args[0] }.isNotEmpty()) {
			guild.emojis.filter { it.name == args[0] }.first()
		} else {
			null
		}
	}

	/**
	 * Sends an embed explaining what the command does
	 *
	 * @param context the context of the command
	 */
	override suspend fun explain() {
		val commandDescription = command.description.invoke(locale)
		val commandLabel = command.labels.first()
		val commandLabelWithPrefix = "${serverConfig.commandPrefix}$commandLabel"

		val embed = EmbedBuilder()
			.setColor(Constants.LORITTA_AQUA)
			.setAuthor(locale["commands.explain.clickHereToSeeAllMyCommands"], "${loritta.config.loritta.website.url}commands", discordMessage.jda.selfUser.effectiveAvatarUrl)
			.setTitle("${Emotes.LORI_HM} `${serverConfig.commandPrefix}${executedCommandLabel}`")
			.setFooter("${user.name + "#" + user.discriminator} • ${command.category.getLocalizedName(locale)}", user.effectiveAvatarUrl)
			.setTimestamp(Instant.now())

		val description = buildString {
			// Builds the "How to Use" string
			this.append(commandDescription)
			this.append('\n')
			this.append('\n')
			this.append("${Emotes.LORI_SMILE} **${locale["commands.explain.howToUse"]}**")
			this.append(" `")
			this.append(commandLabelWithPrefix)
			this.append('`')

			// Only add the arguments if the list is not empty (to avoid adding a empty "` `")
			if (command.usage.arguments.isNotEmpty()) {
				this.append("**")
				this.append('`')
				this.append(' ')
				for ((index, argument) in command.usage.arguments.withIndex()) {
					argument.build(this, locale)

					if (index != command.usage.arguments.size - 1)
						this.append(' ')
				}
				this.append('`')
				this.append("**")

				// If we have arguments with explanations, let's show them!
				val argumentsWithExplanations = command.usage.arguments.filter { it.explanation != null }

				if (argumentsWithExplanations.isNotEmpty()) {
					this.append('\n')
					// Same thing again, but with a *twist*!
					for ((index, argument) in argumentsWithExplanations.withIndex()) {
						this.append("**")
						this.append('`')
						argument.build(this, locale)
						this.append('`')
						this.append("**")
						this.append(' ')

						when (val explanation = argument.explanation) {
							is LocaleKeyData -> {
								this.append(locale.get(explanation))
							}
							is LocaleStringData -> {
								this.append(explanation.text)
							}
							else -> throw IllegalArgumentException("I don't know how to process a $argument!")
						}

						this.append('\n')
					}
				}
			}
		}

		embed.setDescription(description)

		// Create example list
		val examplesKey = command.examplesKey
		val examples = ArrayList<String>()

		if (examplesKey != null) {
			val examplesAsString = locale.getList(examplesKey)

			for (example in examplesAsString) {
				val split = example.split("|-|")
					.map { it.trim() }

				if (split.size == 2) {
					// If the command has a extended description
					// "12 |-| Gira um dado de 12 lados"
					// A extended description can also contain "nothing", but contains a extended description
					// "|-| Gira um dado de 6 lados"
					val (commandExample, explanation) = split

					examples.add("\uD83D\uDD39 **$explanation**")
					examples.add("`" + commandLabelWithPrefix + "`" + (if (commandExample.isEmpty()) "" else "**` $commandExample`**"))
				} else {
					val commandExample = split[0]

					examples.add("`" + commandLabelWithPrefix + "`" + if (commandExample.isEmpty()) "" else "**` $commandExample`**")
				}
			}
		}

		if (examples.isNotEmpty()) {
			embed.addField(
				"\uD83D\uDCD6 ${locale["commands.explain.examples"]}",
				examples.joinToString("\n", transform = { it }),
				false
			)
		}

		val command = command
		if (command is DiscordCommand) {
			if (command.botRequiredPermissions.isNotEmpty() || command.userRequiredPermissions.isNotEmpty()) {
				var field = ""
				if (command.userRequiredPermissions.isNotEmpty()) {
					field += "\uD83D\uDC81 ${locale["commands.explain.youNeedToHavePermission", command.userRequiredPermissions.joinToString(", ", transform = { "`${it.getLocalizedName(i18nContext)}`" })]}\n"
				}
				if (command.botRequiredPermissions.isNotEmpty()) {
					field += "<:loritta:331179879582269451> ${locale["commands.explain.loriNeedToHavePermission", command.botRequiredPermissions.joinToString(", ", transform = { "`${it.getLocalizedName(i18nContext)}`" })]}\n"
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

		val messageBuilder = MessageCreateBuilder()
			.denyMentions(
				Message.MentionType.EVERYONE,
				Message.MentionType.HERE
			)
			.addContent(getUserMention(true))
			.setEmbeds(embed.build())

		discordMessage.channel.sendMessage(messageBuilder.build())
			.referenceIfPossible(discordMessage, serverConfig, true)
			.await()
	}
}