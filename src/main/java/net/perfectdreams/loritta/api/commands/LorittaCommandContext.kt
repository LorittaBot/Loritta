package net.perfectdreams.loritta.api.commands

import com.mrpowergamerbr.loritta.utils.LoriReply
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.core.entities.User
import net.perfectdreams.loritta.api.entities.Message
import java.awt.image.BufferedImage
import java.io.File
import java.io.InputStream

/**
 * Contexto do comando executado
 */
abstract class LorittaCommandContext(val locale: BaseLocale, val legacyLocale: LegacyBaseLocale) {
	abstract val message: Message

	abstract fun getAsMention(addSpace: Boolean): String

	abstract suspend fun reply(message: String, prefix: String? = null, forceMention: Boolean = false): Message

	abstract suspend fun reply(vararg loriReplies: LoriReply): Message
/* 	var metadata = HashMap<String, Any>()

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

	/**
	 * Verifica se o usuário tem permissão para utilizar um comando
	 */
	fun canUseCommand(): Boolean {
		return lorittaUser.canUseCommand(this)
	}

	fun getAsMention(addSpace: Boolean): String {
		return lorittaUser.getAsMention(true) /* if (cmd is AbstractCommand) {
			val cmdOptions = lorittaUser.config.getCommandOptionsFor(cmd as AbstractCommand)
			if (cmdOptions.override) {
				if (cmdOptions.mentionOnCommandOutput)
					lorittaUser.user.asMention + (if (addSpace) " " else "")
				else
					""
			} else lorittaUser.getAsMention(true)
		} else {
			lorittaUser.getAsMention(true)
		} */
	}

	suspend fun reply(message: String, prefix: String? = null, forceMention: Boolean = false): net.perfectdreams.loritta.api.entities.Message {
		var send = ""
		if (prefix != null) {
			send = "$prefix **|** "
		}
		send = send + (if (forceMention) userHandle.asMention + " " else getAsMention(true)) + message
		return sendMessage(send)
	}

	suspend fun reply(vararg loriReplies: LoriReply): net.perfectdreams.loritta.api.entities.Message {
		return reply(false, *loriReplies)
	}

	suspend fun reply(mentionUserBeforeReplies: Boolean, vararg loriReplies: LoriReply): net.perfectdreams.loritta.api.entities.Message {
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

	suspend fun reply(image: BufferedImage, fileName: String, vararg loriReplies: LoriReply): net.perfectdreams.loritta.api.entities.Message {
		val message = StringBuilder()
		for (loriReply in loriReplies) {
			message.append(loriReply.build(this) + "\n")
		}
		return sendFile(image, fileName, message.toString())
	}

	suspend fun sendMessage(message: String): net.perfectdreams.loritta.api.entities.Message {
		return sendMessage(MessageBuilder().append(if (message.isEmpty()) " " else message).build())
	}

	suspend fun sendMessage(message: String, embed: MessageEmbed): net.perfectdreams.loritta.api.entities.Message {
		return sendMessage(MessageBuilder().setEmbed(embed).append(if (message.isEmpty()) " " else message).build())
	}

	suspend fun sendMessage(embed: MessageEmbed): net.perfectdreams.loritta.api.entities.Message {
		return sendMessage(MessageBuilder().append(getAsMention(true)).setEmbed(embed).build())
	}

	suspend fun sendMessage(message: Message): net.perfectdreams.loritta.api.entities.Message {
		var privateReply = lorittaUser.config.commandOutputInPrivate
		/* if (cmd is AbstractCommand) {
			val cmdOptions = lorittaUser.config.getCommandOptionsFor(cmd as AbstractCommand)
			if (cmdOptions.override && cmdOptions.commandOutputInPrivate) {
				privateReply = cmdOptions.commandOutputInPrivate
			}
		} */
		if (privateReply) {
			val privateChannel = lorittaUser.user.openPrivateChannel().await()
			return net.perfectdreams.loritta.api.impl.DiscordMessage(privateChannel.sendMessageAsync(message))
		} else {
			if (isPrivateChannel || event.textChannel!!.canTalk()) {
				val sentMessage = event.channel.sendMessage(message).await()
				if (config.deleteMessagesAfter != null)
					sentMessage.delete().queueAfter(config.deleteMessagesAfter!!, TimeUnit.SECONDS)
				return net.perfectdreams.loritta.api.impl.DiscordMessage(sentMessage)
			} else {
				LorittaUtils.warnOwnerNoPermission(guild, event.textChannel, lorittaUser.config)
				throw RuntimeException("Sem permissão para enviar uma mensagem!")
			}
		}
	}

	suspend fun sendMessage(webhook: TemmieWebhook?, message: DiscordMessage) {
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

	suspend fun sendFile(file: File, name: String, message: String, embed: MessageEmbed? = null): net.perfectdreams.loritta.api.entities.Message {
		// Corrigir erro ao construir uma mensagem vazia
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		if (embed != null)
			builder.setEmbed(embed)
		return sendFile(file, name, builder.build())
	}

	suspend fun sendFile(file: File, name: String, message: Message): net.perfectdreams.loritta.api.entities.Message {
		val inputStream = file.inputStream()
		return sendFile(inputStream, name, message)
	}

	suspend fun sendFile(image: BufferedImage, name: String, embed: MessageEmbed): net.perfectdreams.loritta.api.entities.Message {
		return sendFile(image, name, "", embed)
	}

	suspend fun sendFile(image: BufferedImage, name: String, message: String, embed: MessageEmbed? = null): net.perfectdreams.loritta.api.entities.Message {
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		if (embed != null)
			builder.setEmbed(embed)

		return sendFile(image, name, builder.build())
	}

	suspend fun sendFile(image: BufferedImage, name: String, message: Message): net.perfectdreams.loritta.api.entities.Message {
		val outputStream = ByteArrayOutputStream()
		outputStream.use {
			ImageIO.write(image, "png", it)
		}

		val inputStream = ByteArrayInputStream(outputStream.toByteArray())

		return sendFile(inputStream, name, message)
	}

	suspend fun sendFile(inputStream: InputStream, name: String, message: String): net.perfectdreams.loritta.api.entities.Message {
		// Corrigir erro ao construir uma mensagem vazia
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		return sendFile(inputStream, name, builder.build())
	}

	suspend fun sendFile(inputStream: InputStream, name: String, embed: MessageEmbed): net.perfectdreams.loritta.api.entities.Message {
		return sendFile(inputStream, name, "", embed)
	}

	suspend fun sendFile(inputStream: InputStream, name: String, message: String, embed: MessageEmbed? = null): net.perfectdreams.loritta.api.entities.Message {
		// Corrigir erro ao construir uma mensagem vazia
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		if (embed != null)
			builder.setEmbed(embed)
		return sendFile(inputStream, name, builder.build())
	}

	suspend fun sendFile(inputStream: InputStream, name: String, message: Message): net.perfectdreams.loritta.api.entities.Message {
		var privateReply = lorittaUser.config.commandOutputInPrivate
		/* if (cmd is AbstractCommand) {
			val cmdOptions = lorittaUser.config.getCommandOptionsFor(cmd as AbstractCommand)
			if (cmdOptions.override && cmdOptions.commandOutputInPrivate) {
				privateReply = cmdOptions.commandOutputInPrivate
			}
		} */
		if (privateReply) {
			val privateChannel = lorittaUser.user.openPrivateChannel().await()
			val sentMessage = privateChannel.sendMessageAsync(message)
			inputStream.close()
			return net.perfectdreams.loritta.api.impl.DiscordMessage(sentMessage)
		} else {
			if (isPrivateChannel || event.textChannel!!.canTalk()) {
				val sentMessage = event.channel.sendFile(inputStream, name, message).await()
				inputStream.close()
				if (config.deleteMessagesAfter != null)
					sentMessage.delete().queueAfter(config.deleteMessagesAfter!!, TimeUnit.SECONDS)
				return net.perfectdreams.loritta.api.impl.DiscordMessage(sentMessage)
			} else {
				LorittaUtils.warnOwnerNoPermission(guild, event.textChannel, lorittaUser.config)
				throw RuntimeException("Sem permissão para enviar uma mensagem!")
			}
		}
	}

	/**
	 * Sends an embed explaining what the command does
	 *
	 * @param context the context of the command
	 */
	suspend fun explain() {
		val conf = config
		val ev = event
		val command = cmd

		if (conf.explainOnCommandRun) {
			val rawArguments = message.contentRaw.split(" ")
			var commandLabel = rawArguments[0]
			if (rawArguments.getOrNull(1) != null && (rawArguments[0] == "<@${Loritta.config.clientId}>" || rawArguments[0] == "<@!${Loritta.config.clientId}>")) {
				// Caso o usuário tenha usado "@Loritta comando", pegue o segundo argumento (no caso o "comando") em vez do primeiro (que é a mention da Lori)
				commandLabel = rawArguments[1]
			}
			commandLabel = commandLabel.toLowerCase()

			val embed = EmbedBuilder()
			embed.setColor(Color(0, 193, 223))
			embed.setTitle("\uD83E\uDD14 `$commandLabel`")

			val commandArguments = cmd.getUsage(locale)
			val usage = when {
				commandArguments.arguments.isNotEmpty() -> " `${commandArguments.build(legacyLocale)}`"
				else -> ""
			}

			var cmdInfo = cmd.getDescription(locale) + "\n\n"

			cmdInfo += "\uD83D\uDC81 **" + legacyLocale["HOW_TO_USE"] + ":** " + commandLabel + usage + "\n"

			for (argument in commandArguments.arguments) {
				if (argument.explanation != null) {
					cmdInfo += "${Constants.LEFT_PADDING} `${argument.build(legacyLocale)}` - "
					if (argument.defaultValue != null) {
						cmdInfo += "(Padrão: ${argument.defaultValue}) "
					}
					cmdInfo += "${argument.explanation}\n"
				}
			}

			cmdInfo += "\n"

			// Criar uma lista de exemplos
			val examples = ArrayList<String>()
			for (example in command.getExamples(locale)) { // Adicionar todos os exemplos simples
				examples.add(commandLabel + if (example.isEmpty()) "" else " `$example`")
			}

			if (examples.isEmpty()) {
				embed.addField(
						"\uD83D\uDCD6 " + legacyLocale["EXAMPLE"],
						commandLabel,
						false
				)
			} else {
				var exampleList = ""
				for (example in examples) {
					exampleList += example + "\n"
				}
				embed.addField(
						"\uD83D\uDCD6 " + legacyLocale["EXAMPLE"] + (if (command.getExamples(locale).size == 1) "" else "s"),
						exampleList,
						false
				)
			}

			if (command.botPermissions.isNotEmpty() || command.discordPermissions.isNotEmpty()) {
				var field = ""
				if (command.discordPermissions.isNotEmpty()) {
					field += "\uD83D\uDC81 Você precisa ter permissão para ${command.discordPermissions.joinToString(", ", transform = { "`${it.localized(legacyLocale)}`" })} para utilizar este comando!\n"
				}
				if (command.botPermissions.isNotEmpty()) {
					field += "<:loritta:331179879582269451> Eu preciso de permissão para ${command.botPermissions.joinToString(", ", transform = { "`${it.localized(legacyLocale)}`" })} para poder executar este comando!\n"
				}
				embed.addField(
						"\uD83D\uDCDB Permissões",
						field,
						false
				)
			}

			val aliases = mutableSetOf<String>()
			aliases.addAll(command.labels)

			val onlyUnusedAliases = aliases.filter { it != commandLabel.replaceFirst(config.commandPrefix, "") }
			if (onlyUnusedAliases.isNotEmpty()) {
				embed.addField(
						"\uD83D\uDD00 ${legacyLocale["CommandAliases"]}",
						onlyUnusedAliases.joinToString(", ", transform = { "`" + config.commandPrefix + it + "`" }),
						true
				)
			}

			embed.setDescription(cmdInfo)
			embed.setAuthor("${userHandle.name}#${userHandle.discriminator}", null, ev.author.effectiveAvatarUrl)
			embed.setFooter(legacyLocale[command.category.fancyTitle], "${Loritta.config.websiteUrl}assets/img/loritta_gabizinha_v1.png") // Mostrar categoria do comando
			embed.setTimestamp(Instant.now())

			if (conf.explainInPrivate) {
				ev.author.openPrivateChannel().queue {
					it.sendMessage(embed.build()).queue()
				}
			} else {
				val message = (sendMessage(getAsMention(true), embed.build()) as net.perfectdreams.loritta.api.impl.DiscordMessage).handle
				message.addReaction("❓").queue()
				message.onReactionAddByAuthor(this) {
					if (it.reactionEmote.name == "❓") {
						message.delete().queue()
						explainArguments()
					}
				}
			}
		}
	}

	/**
	 * Sends an embed explaining how the argument works
	 *
	 * @param context the context of the command
	 */
	suspend fun explainArguments() {
		val embed = EmbedBuilder()
		embed.setColor(Color(0, 193, 223))
		embed.setTitle("\uD83E\uDD14 Como os argumentos funcionam?")
		embed.addField(
				"Estilos de Argumentos",
				"""
					`<argumento>` - Argumento obrigatório
					`[argumento]` - Argumento opcional
				""".trimIndent(),
				false
		)

		embed.addField(
				"Tipos de Argumentos",
				"""
					`texto` - Um texto qualquer
					`usuário` - Menção, nome de um usuário ou ID de um usuário
					`imagem` - URL da imagem,  menção, nome de um usuário, ID de um usuário e, caso nada tenha sido encontrado, será pego a primeira imagem encontrada nas últimas 25 mensagens.
				""".trimIndent(),
				false
		)

		val message = (sendMessage(getAsMention(true), embed.build()) as net.perfectdreams.loritta.api.impl.DiscordMessage).handle
		message.addReaction("❓").queue()
		message.onReactionAddByAuthor(this) {
			if (it.reactionEmote.name == "❓") {
				message.delete().queue()
				explain()
			}
		}
	}

	/**
	 * Gets an user from the argument index via mentions, username#oldDiscriminator, effective name, username and user ID
	 *
	 * @param argument the argument index on the rawArgs array
	 * @return         the user object or null, if nothing was found
	 * @see            User
	 */
	suspend fun getUserAt(argument: Int): User? {
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
	 * Gets an user from the argument index via mentions, username#oldDiscriminator, effective name, username and user ID
	 *
	 * @param argument the argument index on the rawArgs array
	 * @return         the user object or null, if nothing was found
	 * @see            User
	 */
	suspend fun getUser(link: String?): net.perfectdreams.loritta.api.entities.User? {
		if (link != null) {
			// Vamos verificar por menções, uma menção do Discord é + ou - assim: <@123170274651668480>
			for (user in this.message.mentionedUsers) {
				if (user.asMention == link.replace("!", "")) { // O replace é necessário já que usuários com nick tem ! no mention (?)
					// Diferente de null? Então vamos usar o avatar do usuário!
					return DiscordUser(user)
				}
			}

			// Vamos tentar procurar pelo username + discriminator
			if (!this.isPrivateChannel && !link.isEmpty()) {
				val split = link.split("#").dropLastWhile { it.isEmpty() }.toTypedArray()

				if (split.size == 2 && split[0].isNotEmpty()) {
					val matchedMember = this.guild.getMembersByName(split[0], false).stream().filter { it -> it.user.discriminator == split[1] }.findFirst()

					if (matchedMember.isPresent) {
						return DiscordUser(matchedMember.get().user)
					}
				}
			}

			// Ok então... se não é link e nem menção... Que tal então verificar por nome?
			if (!this.isPrivateChannel && !link.isEmpty()) {
				val matchedMembers = this.guild.getMembersByEffectiveName(link, true)

				if (!matchedMembers.isEmpty()) {
					return DiscordUser(matchedMembers[0].user)
				}
			}

			// Se não, vamos procurar só pelo username mesmo
			if (!this.isPrivateChannel && !link.isEmpty()) {
				val matchedMembers = this.guild.getMembersByName(link, true)

				if (!matchedMembers.isEmpty()) {
					return DiscordUser(matchedMembers[0].user)
				}
			}

			// Ok, então só pode ser um ID do Discord!
			try {
				val user = LorittaLauncher.loritta.lorittaShards.retrieveUserById(link)

				if (user != null) { // Pelo visto é!
					return DiscordUser(user)
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
	suspend fun getImageUrlAt(argument: Int, search: Int = 25, avatarSize: Int = 2048) = getImageUrl(this.rawArgs.getOrNull(argument))

	/**
	 * Gets an image URL from the argument index via valid URLs at the specified index
	 *
	 * @param argument   the argument index on the rawArgs array
	 * @param search     how many messages will be retrieved from the past to get images (default: 25)
	 * @param avatarSize the size of retrieved user avatars from Discord (default: 2048)
	 * @return           the image URL or null, if nothing was found
	 */
	suspend fun getImageUrl(link: String?, search: Int = 25, avatarSize: Int = 2048): String? {
		if (link != null) {
			if (LorittaUtils.isValidUrl(link))
				return link // Se é um link, vamos enviar para o usuário agora

			// Vamos verificar por usuários no argumento especificado
			val user = getUser(link)
			if (user != null)
				return user.avatarUrl + "?size=" + avatarSize

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
				var unicodeEmoji = LorittaUtils.toUnicode(link.codePointAt(0)) // Vamos usar codepoints porque emojis
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
				val message = this.message.channel.history.retrievePast(search).await()

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
	suspend fun getImageAt(argument: Int, search: Int = 25, avatarSize: Int = 2048): BufferedImage? {
		var toBeDownloaded = getImageUrlAt(argument, 0, avatarSize)

		if (toBeDownloaded == null) {
			if (rawArgs.isNotEmpty()) {
				return ImageUtils.createTextAsImage(256, 256, rawArgs.joinToString(" "))
			}

			toBeDownloaded = getImageUrlAt(argument, search, avatarSize)
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

	/**
	 * Gets an image from the argument index via valid URLs at the specified index
	 *
	 * @param argument   the argument index on the rawArgs array
	 * @param search     how many messages will be retrieved from the past to get images (default: 25)
	 * @param avatarSize the size of retrieved user avatars from Discord (default: 2048)
	 * @return           the image object or null, if nothing was found
	 * @see              BufferedImage
	 */
	suspend fun getImage(text: String, search: Int = 25, avatarSize: Int = 2048): BufferedImage? {
		var toBeDownloaded = getImageUrl(text, 0, avatarSize)

		if (toBeDownloaded == null) {
			if (rawArgs.isNotEmpty()) {
				return ImageUtils.createTextAsImage(256, 256, rawArgs.joinToString(" "))
			}

			toBeDownloaded = getImageUrl(text, search, avatarSize)
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
	} */
	abstract suspend fun reply(mentionUserBeforeReplies: Boolean, vararg loriReplies: LoriReply): Message

	abstract suspend fun reply(image: BufferedImage, fileName: String, vararg loriReplies: LoriReply): Message

	abstract suspend fun sendMessage(message: String): Message

	abstract suspend fun sendFile(file: File, name: String, message: String): Message

	abstract suspend fun sendFile(image: BufferedImage, name: String, message: String): Message

	abstract suspend fun sendFile(inputStream: InputStream, name: String, message: String): Message

	/**
	 * Gets an image from the argument index via valid URLs at the specified index
	 *
	 * @param argument   the argument index on the rawArgs array
	 * @param search     how many messages will be retrieved from the past to get images (default: 25)
	 * @param avatarSize the size of retrieved user avatars from Discord (default: 2048)
	 * @return           the image object or null, if nothing was found
	 * @see              BufferedImage
	 */
	abstract suspend fun getImage(text: String, search: Int = 25, avatarSize: Int = 2048): BufferedImage?

	abstract suspend fun getUserAt(argument: Int): User?

	abstract suspend fun getUser(link: String?): net.perfectdreams.loritta.api.entities.User?

	abstract suspend fun getImageUrlAt(argument: Int, search: Int = 25, avatarSize: Int = 2048): String?

	abstract suspend fun getImageUrl(link: String?, search: Int, avatarSize: Int): String?

	abstract suspend fun getImageAt(argument: Int, search: Int, avatarSize: Int): BufferedImage?

	abstract suspend fun explain()
}