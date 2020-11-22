package net.perfectdreams.loritta.platform.discord.entities

import club.minnced.discord.webhook.WebhookClient
import club.minnced.discord.webhook.send.WebhookMessage
import com.github.kevinsawicki.http.HttpRequest
import com.mrpowergamerbr.loritta.LorittaLauncher
import com.mrpowergamerbr.loritta.dao.ServerConfig
import com.mrpowergamerbr.loritta.events.LorittaMessageEvent
import com.mrpowergamerbr.loritta.utils.*
import com.mrpowergamerbr.loritta.utils.extensions.await
import com.mrpowergamerbr.loritta.utils.extensions.isEmote
import com.mrpowergamerbr.loritta.utils.extensions.localized
import com.mrpowergamerbr.loritta.utils.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.locale.LegacyBaseLocale
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.MessageBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.exceptions.PermissionException
import net.perfectdreams.loritta.api.commands.LorittaCommand
import net.perfectdreams.loritta.api.commands.LorittaCommandContext
import net.perfectdreams.loritta.api.entities.MessageChannel
import net.perfectdreams.loritta.api.messages.LorittaReply
import net.perfectdreams.loritta.api.utils.NoCopyByteArrayOutputStream
import net.perfectdreams.loritta.platform.discord.commands.LorittaDiscordCommand
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAGuild
import net.perfectdreams.loritta.platform.discord.entities.jda.JDAUser
import net.perfectdreams.loritta.utils.DiscordUtils
import net.perfectdreams.loritta.utils.Emotes
import net.perfectdreams.loritta.utils.extensions.build
import org.jsoup.Jsoup
import java.awt.Color
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.time.Instant
import java.util.*
import javax.imageio.ImageIO

class DiscordCommandContext(val config: ServerConfig, val lorittaUser: LorittaUser, locale: BaseLocale, legacyLocale: LegacyBaseLocale, var event: LorittaMessageEvent, command: LorittaCommand, args: Array<String>, val displayArgs: Array<String>, val strippedArgs: Array<String>) : LorittaCommandContext(locale, legacyLocale, command, args) {
	var metadata = HashMap<String, Any>()

	val isPrivateChannel: Boolean
		get() = event.isFromType(ChannelType.PRIVATE)

	override val message: net.perfectdreams.loritta.platform.discord.entities.DiscordMessage
		get() = DiscordMessage(event.message)

	val discordMessage: Message
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

	override val guild: DiscordGuild?
		get() = if (event.guild != null)
			JDAGuild(event.guild!!)
		else
			null

	override val channel: MessageChannel = DiscordMessageChannel(event.channel)

	val discordGuild = event.guild

	/**
	 * Verifica se o usuário tem permissão para utilizar um comando
	 */
	fun canUseCommand(): Boolean {
		return lorittaUser.canUseCommand(this)
	}

	override fun getAsMention(addSpace: Boolean): String {
		return lorittaUser.getAsMention(addSpace) /* if (cmd is AbstractCommand) {
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

	override suspend fun reply(message: String, prefix: String?, forceMention: Boolean): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		var send = ""
		if (prefix != null) {
			send = "$prefix **|** "
		}
		send = send + (if (forceMention) userHandle.asMention + " " else getAsMention(true)) + message
		return sendMessage(send)
	}

	override suspend fun reply(vararg loriReplies: LorittaReply): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		return reply(false, *loriReplies)
	}

	override suspend fun reply(mentionUserBeforeReplies: Boolean, vararg loriReplies: LorittaReply): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		val message = StringBuilder()
		if (mentionUserBeforeReplies) {
			message.append(LorittaReply().build(this))
			message.append("\n")
		}
		for (loriReply in loriReplies) {
			message.append(loriReply.build(this))
			message.append("\n")
		}
		return sendMessage(message.toString())
	}

	override suspend fun reply(image: BufferedImage, fileName: String, vararg loriReplies: LorittaReply): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		val message = StringBuilder()
		for (loriReply in loriReplies) {
			message.append(loriReply.build(this) + "\n")
		}
		return sendFile(image, fileName, message.toString()) as net.perfectdreams.loritta.platform.discord.entities.DiscordMessage
	}

	override suspend fun sendMessage(message: String): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		return sendMessage(MessageBuilder().append(if (message.isEmpty()) " " else message).build())
	}

	suspend fun sendMessage(message: String, embed: MessageEmbed): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		return sendMessage(MessageBuilder().setEmbed(embed).append(if (message.isEmpty()) " " else message).build())
	}

	suspend fun sendMessage(embed: MessageEmbed): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		return sendMessage(MessageBuilder().append(getAsMention(true)).setEmbed(embed).build())
	}

	suspend fun sendMessage(message: Message): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		if (isPrivateChannel || event.textChannel!!.canTalk()) {
			val sentMessage = event.channel.sendMessage(message)
					.reference(discordMessage)
					.await()
			return DiscordMessage(sentMessage)
		} else {
			throw RuntimeException("Sem permissão para enviar uma mensagem!")
		}
	}

	suspend fun sendMessage(webhook: WebhookClient?, message: WebhookMessage) {
		if (!isPrivateChannel && webhook != null) { // Se a webhook é diferente de null, então use a nossa webhook disponível!
			webhook.send(message)
		} else { // Se não, iremos usar embeds mesmo...
			val builder = EmbedBuilder()
			builder.setAuthor(message.username, null, message.avatarUrl)
			builder.setDescription(message.content)
			builder.setFooter("Não consigo usar as permissões de webhook aqui... então estou usando o modo de pobre!", null)

			sendMessage(builder.build())
		}
	}

	override suspend fun sendFile(file: File, name: String, message: String): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		return sendFile(file, name, message, null)
	}

	suspend fun sendFile(file: File, name: String, message: String, embed: MessageEmbed? = null): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		// Corrigir erro ao construir uma mensagem vazia
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		if (embed != null)
			builder.setEmbed(embed)
		return sendFile(file, name, builder.build())
	}

	suspend fun sendFile(file: File, name: String, message: Message): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		val inputStream = file.inputStream()
		return sendFile(inputStream, name, message)
	}

	suspend fun sendFile(image: BufferedImage, name: String, embed: MessageEmbed): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		return sendFile(image, name, "", embed)
	}

	suspend fun sendFile(image: BufferedImage, name: String, message: String, embed: MessageEmbed? = null): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		if (embed != null)
			builder.setEmbed(embed)

		return sendFile(image, name, builder.build())
	}

	suspend fun sendFile(image: BufferedImage, name: String, message: Message): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		val output = NoCopyByteArrayOutputStream()

		ImageIO.write(image, "png", output)

		val inputStream = ByteArrayInputStream(output.toByteArray(), 0, output.size())

		return sendFile(inputStream, name, message)
	}

	override suspend fun sendFile(inputStream: InputStream, name: String, message: String): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		// Corrigir erro ao construir uma mensagem vazia
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		return sendFile(inputStream, name, builder.build())
	}

	suspend fun sendFile(inputStream: InputStream, name: String, embed: MessageEmbed): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		return sendFile(inputStream, name, "", embed)
	}

	suspend fun sendFile(inputStream: InputStream, name: String, message: String, embed: MessageEmbed? = null): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		// Corrigir erro ao construir uma mensagem vazia
		val builder = MessageBuilder()
		builder.append(if (message.isEmpty()) " " else message)
		if (embed != null)
			builder.setEmbed(embed)
		return sendFile(inputStream, name, builder.build())
	}

	suspend fun sendFile(inputStream: InputStream, name: String, message: Message): net.perfectdreams.loritta.platform.discord.entities.DiscordMessage {
		if (isPrivateChannel || event.textChannel!!.canTalk()) {
			val sentMessage = event.channel.sendMessage(message)
					.addFile(inputStream, name)
					.reference(discordMessage).await()
			return DiscordMessage(sentMessage)
		} else {
			throw RuntimeException("Sem permissão para enviar uma mensagem!")
		}
	}

	fun getCommandLabel(): String {
		val rawArguments = discordMessage.contentRaw.split(" ")

		var prefix = config.commandPrefix
		// Como comandos podem ter labels com espaço, é necessário descobrir qual label o usuário usou
		// Por isso iremos fazer igual como é processado comandos
		val checkArguments = rawArguments.toMutableList()
		val rawArgument0 = checkArguments.getOrNull(0)
		val byMention = (rawArgument0 == "<@${loritta.discordConfig.discord.clientId}>" || rawArgument0 == "<@!${loritta.discordConfig.discord.clientId}>")

		if (byMention) {
			checkArguments.removeAt(0)
			prefix = ""
		}

		var commandLabel = "<this is a bug, plz report it>"

		for (label in command.labels) {
			val subLabels = label.split(" ")

			var validLabelCount = 0

			for ((index, subLabel) in subLabels.withIndex()) {
				val rawArgumentAt = checkArguments.getOrNull(index) ?: break

				val subLabelPrefix = if (index == 0)
					prefix
				else
					""

				if (rawArgumentAt.equals(subLabelPrefix + subLabel, true)) { // ignoreCase = true ~ Permite usar "+cOmAnDo"
					validLabelCount++
				}
			}

			if (validLabelCount == subLabels.size) {
				commandLabel = label
				break
			}
		}

		return commandLabel
	}

	/**
	 * Sends an embed explaining what the command does
	 *
	 * @param context the context of the command
	 */
	override suspend fun explain() {
		val serverConfig = config
		val user = userHandle

		val executedCommandLabel = getCommandLabel()

		val embed = EmbedBuilder()
				.setColor(Constants.LORITTA_AQUA)
				.setAuthor(locale["commands.explain.clickHereToSeeAllMyCommands"], "${loritta.instanceConfig.loritta.website.url}commands", discordMessage.jda.selfUser.effectiveAvatarUrl)
				.setTitle("${Emotes.LORI_HM} `${serverConfig.commandPrefix}${executedCommandLabel}`")
				.setFooter("${user.name + "#" + user.discriminator} • ${command.category.getLocalizedName(locale)}", user.effectiveAvatarUrl)
				.setTimestamp(Instant.now())

		val commandArguments = command.getUsage(locale)
		val description = buildString {
			this.append(command.getDescription(locale))
			this.append('\n')
			this.append('\n')
			this.append("${Emotes.LORI_SMILE} **${locale["commands.explain.howToUse"]}** ")
			this.append('`')
			this.append(serverConfig.commandPrefix)
			this.append(command.labels.first())
			this.append('`')
			this.append(' ')
			for ((index, argument) in commandArguments.arguments.withIndex()) {
				// <argumento> - Argumento obrigatório
				// [argumento] - Argumento opcional
				this.append("**")
				this.append('`')
				argument.build(this, locale)
				this.append('`')
				this.append("**")
				if (index != commandArguments.arguments.size - 1)
					this.append(' ')
			}
		}

		embed.setDescription(description)
		val examples = command.getExamples(locale)

		if (examples.isNotEmpty()) {
			embed.addField(
					"\uD83D\uDCD6 ${locale["commands.explain.examples"]}",
					examples.joinToString("\n", transform = { "`${serverConfig.commandPrefix}${executedCommandLabel}` **`${it}`**" }),
					false
			)
		}

		if (command is LorittaDiscordCommand) {
			if (command.botPermissions.isNotEmpty() || command.discordPermissions.isNotEmpty()) {
				var field = ""
				if (command.discordPermissions.isNotEmpty()) {
					field += "\uD83D\uDC81 ${locale["commands.explain.youNeedToHavePermission", command.discordPermissions.joinToString(", ", transform = { "`${it.localized(locale)}`" })]}\n"
				}
				if (command.botPermissions.isNotEmpty()) {
					field += "<:loritta:331179879582269451> ${locale["commands.explain.loriNeedToHavePermission", command.botPermissions.joinToString(", ", transform = { "`${it.localized(locale)}`" })]}\n"
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

		val message = sendMessage(getAsMention(true), embed.build()).handle
		message.addReaction("❓").queue()
		message.onReactionAddByAuthor(this) {
			if (it.reactionEmote.isEmote("❓")) {
				message.delete().queue()
				explainArguments()
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

		val message = sendMessage(getAsMention(true), embed.build()).handle
		message.addReaction("❓").queue()
		message.onReactionAddByAuthor(this) {
			if (it.reactionEmote.isEmote("❓")) {
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
	override suspend fun getUserAt(argument: Int) = this.args.getOrNull(argument)
			?.let {
				DiscordUtils.extractUserFromString(
						it,
						message.handle.mentionedUsers,
						if (isPrivateChannel) null else message.handle.guild
				)
			}

	/**
	 * Gets an user from the argument index via mentions, username#oldDiscriminator, effective name, username and user ID
	 *
	 * @param argument the argument index on the rawArgs array
	 * @return         the user object or null, if nothing was found
	 * @see            User
	 */
	override suspend fun getUser(link: String?): net.perfectdreams.loritta.api.entities.User? {
		if (link != null) {
			// Vamos verificar por menções, uma menção do Discord é + ou - assim: <@123170274651668480>
			for (user in this.discordMessage.mentionedUsers) {
				if (user.asMention == link.replace("!", "")) { // O replace é necessário já que usuários com nick tem ! no mention (?)
					// Diferente de null? Então vamos usar o avatar do usuário!
					return JDAUser(user)
				}
			}

			// Vamos tentar procurar pelo username + discriminator
			if (this.guild is DiscordGuild) {
				val handle = (this.guild as JDAGuild).handle
				if (!this.isPrivateChannel && !link.isEmpty()) {
					val split = link.split("#").dropLastWhile { it.isEmpty() }.toTypedArray()

					if (split.size == 2 && split[0].isNotEmpty()) {
						val matchedMember = handle.getMembersByName(split[0], false).stream().filter { it -> it.user.discriminator == split[1] }.findFirst()

						if (matchedMember.isPresent) {
							return JDAUser(matchedMember.get().user)
						}
					}
				}

				// Ok então... se não é link e nem menção... Que tal então verificar por nome?
				if (!this.isPrivateChannel && !link.isEmpty()) {
					val matchedMembers = handle.getMembersByEffectiveName(link, true)

					if (!matchedMembers.isEmpty()) {
						return JDAUser(matchedMembers[0].user)
					}
				}

				// Se não, vamos procurar só pelo username mesmo
				if (!this.isPrivateChannel && !link.isEmpty()) {
					val matchedMembers = handle.getMembersByName(link, true)

					if (!matchedMembers.isEmpty()) {
						return JDAUser(matchedMembers[0].user)
					}
				}
			}

			// Ok, então só pode ser um ID do Discord!
			try {
				val user = LorittaLauncher.loritta.lorittaShards.retrieveUserById(link)

				if (user != null) { // Pelo visto é!
					return JDAUser(user)
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
	override suspend fun getImageUrlAt(argument: Int, search: Int, avatarSize: Int) = getImageUrl(this.args.getOrNull(argument), search, avatarSize)

	/**
	 * Gets an image URL from the argument index via valid URLs at the specified index
	 *
	 * @param argument   the argument index on the rawArgs array
	 * @param search     how many messages will be retrieved from the past to get images (default: 25)
	 * @param avatarSize the size of retrieved user avatars from Discord (default: 2048)
	 * @return           the image URL or null, if nothing was found
	 */
	override suspend fun getImageUrl(link: String?, search: Int, avatarSize: Int): String? {
		if (link != null) {
			if (LorittaUtils.isValidUrl(link))
				return link // Se é um link, vamos enviar para o usuário agora

			// Vamos verificar por usuários no argumento especificado
			val user = getUser(link) as DiscordUser?
			if (user != null)
				return user.effectiveAvatarUrl + "?size=" + avatarSize

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
				var unicodeEmoji = LorittaUtils.toUnicode(link.codePointAt(0)) // Vamos usar codepoints porque emojis
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

		if (this.guild is JDAGuild) {
			val handle = (this.guild as JDAGuild).handle
			// Ainda nada válido? Quer saber, desisto! Vamos pesquisar as mensagens antigas deste servidor & embeds então para encontrar attachments...
			if (search > 0 && !this.isPrivateChannel && handle.selfMember.hasPermission(this.event.textChannel!!, Permission.MESSAGE_HISTORY)) {
				try {
					val message = this.discordMessage.channel.history.retrievePast(search).await()

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
	override suspend fun getImageAt(argument: Int, search: Int, avatarSize: Int): BufferedImage? {
		var toBeDownloaded = getImageUrlAt(argument, 0, avatarSize)

		if (toBeDownloaded == null) {
			if (args.isNotEmpty()) {
				return ImageUtils.createTextAsImage(256, 256, args.joinToString(" "))
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
	override suspend fun getImage(text: String, search: Int, avatarSize: Int): BufferedImage? {
		var toBeDownloaded = getImageUrl(text, 0, avatarSize)

		if (toBeDownloaded == null) {
			if (args.isNotEmpty()) {
				return ImageUtils.createTextAsImage(256, 256, args.joinToString(" "))
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
	}
	/**
	 *
	 * Gets a role from argument index
	 *
	 * @param argument the argument index
	 * @see Role
	 * */
	fun getRoleAt(argument: Int): Role? {
		if (this.args.size > argument) {
			val roleStr = this.args[argument]
			val guild = this.discordGuild!!
			val message = this.discordMessage

			try {
				// Tentar encontrar por cargos mencionados
				val mentionedRole = message.mentionedRoles.getOrNull(argument)

				if (mentionedRole != null)
					return mentionedRole

				// Tentar encontrar por ID de cargo
				if (roleStr.isValidSnowflake() && guild.getRoleById(roleStr) != null)
					return guild.getRoleById(roleStr)

				// Tentar encontrar por nome
				val rolesMatchingName = guild.getRolesByName(roleStr, true)
				if (rolesMatchingName.isNotEmpty())
					return rolesMatchingName.first()
			} catch(e: Exception) {
				// Se for algum motivo der erro, apenas retorne null
				return null
			}
		}
		return null
	}
}