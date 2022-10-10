package net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands

import com.github.kevinsawicki.http.HttpRequest
import dev.kord.common.entity.AllowedMentionType
import dev.kord.common.entity.ChannelType
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import dev.kord.common.entity.Permission
import dev.kord.rest.request.KtorRequestException
import net.perfectdreams.loritta.deviousfun.entities.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.discord.utils.RawToFormated.toLocalized
import net.perfectdreams.loritta.morenitta.api.commands.Command
import net.perfectdreams.loritta.common.utils.image.Image
import net.perfectdreams.loritta.common.utils.image.JVMImage
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.locale.LocaleKeyData
import net.perfectdreams.loritta.common.locale.LocaleStringData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.DiscordMessage
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.entities.jda.JDAUser
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import net.perfectdreams.loritta.common.utils.Emotes
import net.perfectdreams.loritta.deviousfun.*
import net.perfectdreams.loritta.morenitta.api.commands.CommandContext
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
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
	val isPrivateChannel = discordMessage.channelType == ChannelType.DM
	val guild: Guild
		get() = discordMessage.guild!! // TODO - DeviousFun (it wasn't nullable before)
	val user = discordMessage.author
	val member = discordMessage.member

	suspend fun sendMessage(message: String, embed: DeviousEmbed): Message {
		return sendMessage(MessageBuilder()
			.denyMentions(
				AllowedMentionType.EveryoneMentions
			)
			.setEmbed(embed)
			.append(if (message.isEmpty()) " " else message)
			.build()
		)
	}

	suspend fun sendMessage(embed: DeviousEmbed): Message {
		return sendMessage(MessageBuilder()
			.denyMentions(
				AllowedMentionType.EveryoneMentions
			)
			.append(getUserMention(true))
			.setEmbed(embed)
			.build()
		)
	}

	suspend fun sendMessage(message: DeviousMessage): Message {
		if (isPrivateChannel || discordMessage.textChannel.canTalk()) {
			return discordMessage.channel.sendMessage(
				MessageBuilder(message)
					.referenceIfPossible(discordMessage, serverConfig, true)
					.build()
			).await()
		} else {
			throw RuntimeException("Sem permissão para enviar uma mensagem!")
		}
	}

	override suspend fun sendImage(image: Image, fileName: String, content: String): net.perfectdreams.loritta.morenitta.api.entities.Message {
		return DiscordMessage(
			discordMessage.channel.sendMessage(
				MessageBuilder(content)
					.addFile(image.toByteArray(), fileName)
					.referenceIfPossible(discordMessage, serverConfig, true)
					.build()
			).await()
		)
	}

	override suspend fun sendFile(byteArray: ByteArray, fileName: String, content: String): net.perfectdreams.loritta.morenitta.api.entities.Message {
		return DiscordMessage(
			discordMessage.channel.sendMessage(
				MessageBuilder(content)
					.addFile(byteArray, fileName)
					.referenceIfPossible(discordMessage, serverConfig, true)
					.build()
			)
		)
	}

	suspend fun sendFile(file: File, fileName: String, content: String = this.getUserMention(true), embed: DeviousEmbed? = null): DiscordMessage {
		return DiscordMessage(
			discordMessage.channel.sendMessage(
				MessageBuilder()
					.denyMentions(
						AllowedMentionType.EveryoneMentions
					)
					.append(content)
					.setEmbed(embed)
					.addFile(file, fileName)
					.referenceIfPossible(discordMessage, serverConfig, true)
					.build()
			).await()
		)
	}

	suspend fun sendFile(inputStream: InputStream, fileName: String, content: String = this.getUserMention(true), embed: DeviousEmbed? = null): DiscordMessage {
		return DiscordMessage(
			discordMessage.channel.sendMessage(
				MessageBuilder()
					.denyMentions(
						AllowedMentionType.EveryoneMentions
					)
					.append(content)
					.setEmbed(embed)
					.addFile(inputStream.readAllBytes(), fileName)
					.referenceIfPossible(discordMessage, serverConfig, true)
					.build()
			)
				.await()
		)
	}

	override suspend fun user(argument: Int) = this.args.getOrNull(argument)
		?.let {
			DiscordUtils.extractUserFromString(
				loritta,
				it,
				discordMessage.mentionedUsers,
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
			for (emote in this.discordMessage.emotes) {
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
				val toBeDownloaded = "https://twemoji.maxcdn.com/2/72x72/$unicodeEmoji.png"
				if (HttpRequest.get(toBeDownloaded).code() == 200) {
					return toBeDownloaded
				}
			} catch (e: Exception) {
			}
		}

		// Nothing found? Try retrieving the replied message content
		if (!this.isPrivateChannel && this.guild.selfMemberHasPermission(this.discordMessage.textChannel, Permission.ReadMessageHistory)) {
			val referencedMessage = discordMessage.retrieveReferencedMessage()
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
		if (searchPreviousMessages > 0 && !this.isPrivateChannel && guild.selfMemberHasPermission(discordMessage.channel, Permission.ReadMessageHistory)) {
			val textChannel = discordMessage.channel
			try {
				// TODO - DeviousFun
				val messages = textChannel.history.retrievePast(searchPreviousMessages).await()

				attach@ for (msg in messages) {
					for (embed in msg.embeds) {
						if (embed.image != null && loritta.connectionManager.isTrusted(embed.image!!.url!!))
							return embed.image!!.url
					}
					for (attachment in msg.attachments) {
						if (attachment.isImage && loritta.connectionManager.isTrusted(attachment.url))
							return attachment.url
					}
				}
			} catch (e: KtorRequestException) {
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

	fun textChannel(argument: Int): Channel? {
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

	fun voiceChannel(argument: Int): Channel? {
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

	fun emote(argument: Int): DiscordGuildEmote? {
		val regexEmote = Regex("(<)|[a-z]|(_)|(:)|(>)")
		val emoteId = args.getOrNull(argument)?.let { regexEmote.replace(it, "") }

		return if (emoteId?.isValidSnowflake()!!) {
			guild.getEmoteById(emoteId)
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
			.setAuthor(locale["commands.explain.clickHereToSeeAllMyCommands"], "${loritta.config.loritta.website.url}commands", discordMessage.deviousFun.retrieveSelfUser().effectiveAvatarUrl)
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
					field += "\uD83D\uDC81 ${locale["commands.explain.youNeedToHavePermission", command.userRequiredPermissions.toSet().toLocalized()?.joinToString(", ", transform = { "`${i18nContext.get(it)}`" })]}\n"
				}
				if (command.botRequiredPermissions.isNotEmpty()) {
					field += "<:loritta:331179879582269451> ${locale["commands.explain.loriNeedToHavePermission", command.botRequiredPermissions.toSet().toLocalized()?.joinToString(", ", transform = { "`${i18nContext.get(it)}`" })]}\n"
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
			.denyMentions(
				AllowedMentionType.EveryoneMentions
			)
			.append(getUserMention(true))
			.setEmbed(embed.build())

		discordMessage.channel.sendMessage(
			messageBuilder
				.referenceIfPossible(discordMessage, serverConfig, true)
				.build()
		).await()
	}
}