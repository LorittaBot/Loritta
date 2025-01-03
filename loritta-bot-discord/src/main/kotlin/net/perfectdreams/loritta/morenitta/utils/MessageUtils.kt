package net.perfectdreams.loritta.morenitta.utils

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.serialization.SerializationException
import mu.KotlinLogging
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.events.message.react.GenericMessageReactionEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionRemoveEvent
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.Component
import net.dv8tion.jda.api.interactions.components.ItemComponent
import net.dv8tion.jda.api.interactions.components.LayoutComponent
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.*
import net.perfectdreams.loritta.common.utils.embeds.DiscordComponent
import net.perfectdreams.loritta.common.utils.embeds.DiscordMessage
import net.perfectdreams.loritta.common.utils.placeholders.*
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.linkButton
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.morenitta.utils.extensions.isValidUrl
import net.perfectdreams.loritta.morenitta.utils.placeholders.RenderableMessagePlaceholder
import kotlin.collections.set

object MessageUtils {
	private val logger = KotlinLogging.logger {}
	private val CHAT_EMOJI_REGEX = Regex("(?<!<a?):([A-z0-9_]+):")

	/**
	 * Watermarks the message with a user mention, to avoid ToS issues affecting Loritta with "anonymous message sends"
	 *
	 * While [watermarkSayMessage] watermarks the end of the message or watermarks the footer of a message, this
	 * watermarks the beginning of the message, no matter if it has a embed or not.
	 *
	 * @param message       the message content itself, can be a Discord Message in JSON format as String
	 * @param locale        the locale of the guild
	 * @param guild         the guild that caused the message to be sent
	 * @param watermarkType the string containing the watermark type of the message
	 * @return              a Discord Message in JSON format as a String with a watermarked
	 */
	fun watermarkModuleMessage(message: String, locale: BaseLocale, guild: Guild, watermarkType: String): String {
		val jsonObject = try {
			JsonParser.parseString(message).obj
		} catch (ex: Exception) {
			// If it is not a valid JSON Message, let's create a JSON with the message content
			jsonObject(
				"content" to message
			)
		}

		val watermarkMessage = "> ${Emotes.LORI_COFFEE} *${locale["loritta.moduleDirectMessageConfiguredBy", "${guild.name} `(${guild.id})`", watermarkType]}*\n\n"
		val originalContent = jsonObject["content"]
			.nullString ?: ""

		jsonObject["content"] = watermarkMessage + originalContent.substringIfNeeded(
			range = 0 until (2000 - watermarkMessage.length)
		)

		return jsonObject.toString()
	}

	/**
	 * Watermarks the message with a user mention, to avoid ToS issues affecting Loritta with "anonymous message sends"
	 *
	 * @param message          the message content itself, can be a Discord Message in JSON format as String
	 * @param watermarkForUser the user that this message is going to be watermarked with
	 * @param watermarkText    the text that should be watermarked for, {0} will be replaced with the user's mention
	 * @return                 a Discord Message in JSON format as a String with a watermarked
	 */
	fun watermarkSayMessage(message: String, watermarkForUser: User, watermarkText: String): String {
		val jsonObject = try {
			JsonParser.parseString(message).obj
		} catch (ex: Exception) {
			// If it is not a valid JSON Message, let's create a JSON with the message content
			jsonObject(
				"content" to message
			)
		}

		var isWatermarked = false

		val messageEmbed = jsonObject["embed"]
			.nullObj

		if (messageEmbed != null) {
			val footer = messageEmbed["footer"]
				.nullObj

			if (footer == null) {
				// If the message has an embed, but doesn't have a footer, place the watermark on the embed's footer!
				isWatermarked = true
				messageEmbed["footer"] = jsonObject(
					"text" to watermarkForUser.name + "#" + watermarkForUser.discriminator + " (${watermarkForUser.idLong})",
					"icon_url" to watermarkForUser.effectiveAvatarUrl
				)
			}
		}

		if (!isWatermarked) {
			// If the message isn't watermarked yet, let's place the watermark on the content itself
			isWatermarked = true
			val watermarkMessage = "\n\n${Emotes.LORI_COFFEE} *${watermarkText.format(watermarkForUser.asMention)}*"
			val originalContent = jsonObject["content"]
				.nullString ?: ""

			jsonObject["content"] = originalContent.substringIfNeeded(
				range = 0 until (2000 - watermarkMessage.length)
			) + watermarkMessage
		}

		return jsonObject.toString()
	}

	fun <T : MessagePlaceholder> generateMessage(message: String, guild: Guild?, section: SectionPlaceholders<T>, customTokensBuilder: (T) -> (String), safe: Boolean = true): MessageCreateData {
		val customTokens = mutableListOf<RenderableMessagePlaceholder>()
		section.placeholders.forEach {
			val placeholderValue = customTokensBuilder.invoke(it)
			customTokens.add(RenderableMessagePlaceholder(it, placeholderValue))
		}
		return generateMessage(message, guild, customTokens, safe)
	}

	fun generateMessage(message: String, guild: Guild?, customTokens: List<RenderableMessagePlaceholder> = listOf(), safe: Boolean = true): MessageCreateData {
		val originalDiscordMessage = try {
			JsonIgnoreUnknownKeys.decodeFromString<DiscordMessage>(message)
		} catch (e: SerializationException) {
			DiscordMessage(content = message) // If the message is null, use the message as the content!
		} catch (e: IllegalStateException) {
			DiscordMessage(content = message) // This may be triggered when a message has invalid message components
		}

		fun recursiveComponentReplacer(component: DiscordComponent): DiscordComponent {
			return when (component) {
				is DiscordComponent.DiscordActionRow -> {
					with(component) {
						component.copy(
							components = this.components.map { recursiveComponentReplacer(it) }
						)
					}
				}

				is DiscordComponent.DiscordButton -> {
					with(component) {
						component.copy(
							label = processStringAndReplaceTokens(this.label, 80, guild, customTokens),
							url = replaceTokens(this.url, guild, customTokens)
						)
					}
				}
			}
		}

		// Let's replace all the tokens!
		val discordMessage = with(originalDiscordMessage) {
			copy(
				content = processStringAndReplaceTokens(content, 2000, guild, customTokens),
				embed = embed?.let {
					with(it) {
						this.copy(
							author = with(author) {
								this?.copy(
									name = processStringAndReplaceTokens(this.name, 256, guild, customTokens),
									url = processUrlIfNotNull(replaceTokensIfNotNull(url, guild, customTokens)),
									iconUrl = processUrlIfNotNull(
										replaceTokensIfNotNull(
											iconUrl,
											guild,
											customTokens
										)
									)
								)
							},
							title = processStringAndReplaceTokensIfNotNull(title, 256, guild, customTokens),
							description = processStringAndReplaceTokensIfNotNull(
								description,
								4096,
								guild,
								customTokens
							),
							url = processUrlIfNotNull(replaceTokensIfNotNull(url, guild, customTokens)),
							footer = with(footer) {
								this?.copy(
									text = processStringAndReplaceTokens(text, 2048, guild, customTokens),
									iconUrl = processImageUrlIfNotNull(
										replaceTokensIfNotNull(
											iconUrl,
											guild,
											customTokens
										)
									)
								)
							},
							image = with(image) {
								this?.copy(
									url = processImageUrl(replaceTokens(url, guild, customTokens))
								)
							},
							thumbnail = with(thumbnail) {
								this?.copy(
									url = processImageUrl(replaceTokens(url, guild, customTokens))
								)
							},
							fields = fields.map {
								it.copy(
									name = processStringAndReplaceTokens(it.name, 256, guild, customTokens),
									value = processStringAndReplaceTokens(it.value, 1024, guild, customTokens),
									inline = it.inline
								)
							}
						)
					}
				},
				components = components?.map { recursiveComponentReplacer(it) }
			)
		}

		val messageBuilder = MessageCreateBuilder()
		messageBuilder.setContent(discordMessage.content)
		val discordEmbed = discordMessage.embed
		if (discordEmbed != null) {
			val embed = EmbedBuilder()
				.setAuthor(discordEmbed.author?.name, discordEmbed.author?.url, discordEmbed.author?.iconUrl)
				.setTitle(discordEmbed.title, discordEmbed.url)
				.setDescription(discordEmbed.description)
				.setThumbnail(discordEmbed.thumbnail?.url)
				.setImage(discordEmbed.image?.url)
				.setFooter(discordEmbed.footer?.text, discordEmbed.footer?.iconUrl)

			for (field in discordEmbed.fields) {
				embed.addField(field.name, field.value, field.inline)
			}

			val color = discordEmbed.color
			if (color != null)
				embed.setColor(color)

			try {
				messageBuilder.setEmbeds(embed.build())
			} catch (e: Exception) {
				// Creating a empty embed can cause errors, so we just wrap it in a try .. catch block and hope
				// for the best!
			}
		}

		fun recursiveComponentConverter(component: DiscordComponent): Component {
			return when (component) {
				is DiscordComponent.DiscordActionRow -> {
					// ActionRows cannot have other ActionRows within them so whatever
					ActionRow.of(component.components.map { recursiveComponentConverter(it) as ItemComponent })
				}

				is DiscordComponent.DiscordButton -> {
					// We ONLY support link buttons
					Button.of(
						ButtonStyle.LINK,
						component.url,
						component.label
					)
				}
			}
		}

		val components = discordMessage.components
		if (components != null) {
			for (component in components) {
				// The first component must be a layout component (also known as... ActionRow)
				// If it isn't, then the JSON is invalid!
				messageBuilder.addComponents(recursiveComponentConverter(component) as LayoutComponent)
			}
		}

		return messageBuilder.build()
	}

	// This is still used by "legacy" (let's be honest, it ain't legacy lmao) modules and commands
	// Let's remap it to the new version!
	fun generateMessage(message: String, sources: List<Any>?, guild: Guild?, customTokens: Map<String, String> = mutableMapOf(), safe: Boolean = true): MessageCreateData {
		val tokens = mutableMapOf<String, String?>()
		tokens.putAll(customTokens)

		if (sources != null) {
			for (source in sources) {
				if (source is User) {
					tokens[Placeholders.USER_MENTION.name] = source.asMention
					tokens[Placeholders.USER_NAME_SHORT.name] = source.globalName ?: source.name
					tokens[Placeholders.USER_NAME.name] = source.globalName ?: source.name
					tokens[Placeholders.USER_DISCRIMINATOR.name] = source.discriminator
					tokens[Placeholders.USER_ID.name] = source.id
					tokens[Placeholders.USER_AVATAR_URL.name] = source.effectiveAvatarUrl
					tokens[Placeholders.USER_TAG.name] = "@${source.name.escapeMentions()}"

					tokens[Placeholders.Deprecated.USER_DISCRIMINATOR.name] = source.discriminator
					tokens[Placeholders.Deprecated.USER_ID.name] = source.id
					tokens[Placeholders.Deprecated.USER_AVATAR_URL.name] = source.effectiveAvatarUrl
				}
				if (source is Member) {
					tokens[Placeholders.USER_MENTION.name] = source.asMention
					tokens[Placeholders.USER_NAME_SHORT.name] = source.user.globalName ?: source.user.name
					tokens[Placeholders.USER_NAME.name] = source.user.globalName ?: source.user.name
					tokens[Placeholders.USER_DISCRIMINATOR.name] = source.user.discriminator
					tokens[Placeholders.USER_ID.name] = source.id
					tokens[Placeholders.USER_TAG.name] = "@${source.user.name.escapeMentions()}"
					tokens[Placeholders.USER_AVATAR_URL.name] = source.user.effectiveAvatarUrl
					tokens[Placeholders.USER_NICKNAME.name] = source.effectiveName

					tokens[Placeholders.Deprecated.USER_DISCRIMINATOR.name] = source.user.discriminator
					tokens[Placeholders.Deprecated.USER_ID.name] = source.id
					tokens[Placeholders.Deprecated.USER_AVATAR_URL.name] = source.user.effectiveAvatarUrl
					tokens[Placeholders.Deprecated.USER_NICKNAME.name] = source.effectiveName
				}
				if (source is Guild) {
					val guildSize = source.memberCount.toString()
					val mentionOwner = source.owner?.asMention ?: "???"
					val owner = source.owner?.effectiveName ?: "???"


					tokens[Placeholders.GUILD_NAME_SHORT.name] = source.name
					tokens[Placeholders.GUILD_NAME.name] = source.name
					tokens[Placeholders.GUILD_SIZE.name] = guildSize
					tokens[Placeholders.GUILD_ICON_URL.name] = source.iconUrl?.replace("jpg", "png")

					// Deprecated stuff
					tokens["guildsize"] = guildSize
					tokens["guild-size"] = guildSize
					tokens["@owner"] = mentionOwner
					tokens["owner"] = owner
					tokens["guild-icon-url"] = source.iconUrl?.replace("jpg", "png")
				}
				if (source is GuildChannel) {
					tokens["channel"] = source.name
					tokens["channel-id"] = source.id
				}
				if (source is TextChannel) {
					tokens["@channel"] = source.asMention
				}
			}
		}

		return generateMessage(
			message,
			guild,
			tokens.filter { it.value != null }.map {
				RenderableMessagePlaceholder(
					GenericPlaceholders.Placeholder(
						listOf(LorittaPlaceholder(it.key).toVisiblePlaceholder())
					),
					it.value!!
				)
			}
		)
	}

	// This is the refactored version
	fun <T : MessagePlaceholder> generateMessageOrFallbackIfInvalid(i18nContext: I18nContext, message: String, guild: Guild?, section: SectionPlaceholders<T>, customTokensBuilder: (T) -> (String), generationErrorMessageI18nKey: StringI18nData, safe: Boolean = true): MessageCreateData {
		val customTokens = mutableListOf<RenderableMessagePlaceholder>()
		section.placeholders.forEach {
			val placeholderValue = customTokensBuilder.invoke(it)
			customTokens.add(RenderableMessagePlaceholder(it, placeholderValue))
		}
		return generateMessageOrFallbackIfInvalid(
			i18nContext,
			message,
			guild,
			customTokens,
			generationErrorMessageI18nKey,
			safe
		)
	}

	fun generateMessageOrFallbackIfInvalid(
		i18nContext: I18nContext,
		message: String,
		guild: Guild?,
		customTokens: List<RenderableMessagePlaceholder> = listOf(),
		generationErrorMessageI18nKey: StringI18nData,
		safe: Boolean = true
	) = generateMessageOrFallbackIfInvalid(
		i18nContext,
		message,
		guild,
		customTokens,
		i18nContext.get(generationErrorMessageI18nKey),
		safe
	)

	fun generateMessageOrFallbackIfInvalid(
		i18nContext: I18nContext,
		message: String,
		guild: Guild?,
		customTokens: List<RenderableMessagePlaceholder> = listOf(),
		generationErrorMessage: String,
		safe: Boolean = true
	): MessageCreateData {
		return try {
			generateMessage(
				message,
				guild,
				customTokens,
				safe
			)
		} catch (e: Exception) {
			logger.warn(e) { "Something went wrong while trying to generate message! Falling back..." }
			createFallbackMessage(i18nContext, generationErrorMessage)
		}
	}

	// This is the legacy version
	fun generateMessageOrFallbackIfInvalid(
		i18nContext: I18nContext,
		message: String,
		sources: List<Any>?,
		guild: Guild?,
		customTokens: Map<String, String> = mutableMapOf(),
		generationErrorMessageI18nKey: StringI18nData,
		safe: Boolean = true
	) = generateMessageOrFallbackIfInvalid(
		i18nContext,
		message,
		sources,
		guild,
		customTokens,
		i18nContext.get(generationErrorMessageI18nKey),
		safe
	)

	fun generateMessageOrFallbackIfInvalid(i18nContext: I18nContext, message: String, sources: List<Any>?, guild: Guild?, customTokens: Map<String, String> = mutableMapOf(), generationErrorMessage: String, safe: Boolean = true): MessageCreateData {
		return try {
			generateMessage(
				message,
				sources,
				guild,
				customTokens,
				safe
			)
		} catch (e: Exception) {
			logger.warn(e) { "Something went wrong while trying to generate message! Falling back..." }
			createFallbackMessage(i18nContext, generationErrorMessage)
		}
	}

	/**
	 * Creates a fallback message for when [MessageUtils.generateMessage] fails to generate due to an error in the message
	 */
	private fun createFallbackMessage(i18nContext: I18nContext, generationErrorMessage: String): MessageCreateData {
		return MessageCreate {
			embed {
				title = "${net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriSob} ${i18nContext.get(I18nKeysData.InvalidMessages.InvalidMessage)}"
				description = i18nContext.get(I18nKeysData.InvalidMessages.Base(generationErrorMessage))
				color = LorittaColors.LorittaRed.rgb
				footer(i18nContext.get(I18nKeysData.InvalidMessages.IsItTooLateNowToSaySorry))
			}

			actionRow(
				linkButton(
					GACampaigns.createUrlWithCampaign(
						"https://loritta.website/dashboard",
						"discord",
						"loritta-info",
						"loritta-info-links",
						"dashboard"
					).toString(),
					i18nContext.get(I18nKeysData.Commands.Command.Loritta.Info.Dashboard),
					net.perfectdreams.loritta.cinnamon.emotes.Emotes.LoriReading
				)
			)
		}
	}

	private fun replaceTokens(text: String, guild: Guild?, customTokens: List<RenderableMessagePlaceholder>): String {
		var message = text

		for ((token, value) in customTokens)
			for (name in token.names.map { it.placeholder.name })
				message = message.replace("{$name}", value)

		// Para evitar pessoas perguntando "porque os emojis não funcionam???", nós iremos dar replace automaticamente em algumas coisas
		// para que elas simplesmente "funcionem:tm:"
		// Ou seja, se no chat do Discord aparece corretamente, é melhor que na própria Loritta também apareça, não é mesmo?
		if (guild != null) {
			val emojis = guild.emojis

			// Emojis are kinda tricky, we need to match
			// :lori_clown:
			// but not
			// <:lori_clown:950111543574536212>
			// but that's hard, so how can we do this?
			// ...
			// with the power of RegEx of course! :3
			message = message.replace(CHAT_EMOJI_REGEX) {
				val emojiName = it.groupValues[1]
				val guildEmoji = emojis.firstOrNull { it.name == emojiName }
				if (guildEmoji != null) {
					buildString {
						append("<")
						if (guildEmoji.isAnimated)
							append("a")
						append(":")
						append(guildEmoji.name)
						append(":")
						append(guildEmoji.id)
						append(">")
					}
				} else {
					it.value // Emoji wasn't found, so let's keep it as is
				}
			}

			// Before we did replace channel names/roles with proper mentions in the message
			// However, that causes a lot of issues: What if there's a role WITHOUT a name? Then every "@" is replaced!
			// What if someone wants to link Loritta's YouTube channel? Then the message will replace "@Loritta" with the role mention!
			// That's baaaaad, so let's just... not do it.
		}

		return message
	}

	private fun replaceTokensIfNotNull(text: String?, guild: Guild?, customTokens: List<RenderableMessagePlaceholder>) = text?.let { replaceTokens(text, guild, customTokens) }

	private fun processStringAndReplaceTokens(text: String, maxSize: Int, guild: Guild?, customTokens: List<RenderableMessagePlaceholder>) = processString(replaceTokens(text, guild, customTokens), maxSize)

	private fun processStringAndReplaceTokensIfNotNull(text: String?, maxSize: Int, guild: Guild?, customTokens: List<RenderableMessagePlaceholder>) = text?.let { processString(replaceTokens(it, guild, customTokens), maxSize) }

	private fun processStringIfNotNull(text: String?, maxSize: Int) = text?.let { processString(it, maxSize) }

	private fun processString(text: String, maxSize: Int) = text.substringIfNeeded(0 until maxSize)

	private fun processImageUrl(url: String): String {
		if (!url.isValidUrl())
			return Constants.INVALID_IMAGE_URL
		return url
	}

	private fun processImageUrlIfNotNull(url: String?): String? {
		if (url != null && !url.isValidUrl())
			return Constants.INVALID_IMAGE_URL
		return url
	}

	private fun processUrlIfNotNull(url: String?): String? {
		if (url != null && !url.isValidUrl())
			return null
		return url
	}
}

/**
 * When an user adds a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionAdd(context: CommandContext, function: suspend (MessageReactionAddEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = context.loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onReactionAdd = function
	return this
}

/**
 * When an user adds a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionAdd(context: DiscordCommandContext, function: suspend (MessageReactionAddEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = context.loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.user.idLong) }
	functions.onReactionAdd = function
	return this
}

/**
 * When an user adds a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionAdd(context: UnleashedContext, function: suspend (MessageReactionAddEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = context.loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.user.idLong) }
	functions.onReactionAdd = function
	return this
}

/**
 * When an user removes a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionRemove(context: CommandContext, function: suspend (MessageReactionRemoveEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = context.loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onReactionRemove = function
	return this
}

/**
 * When the command executor adds a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionAddByAuthor(context: CommandContext, function: suspend (MessageReactionAddEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = context.loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onReactionAddByAuthor = function
	return this
}

/**
 * When the command executor adds a reaction to this message
 *
 * @param userId   the user ID
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionAddByAuthor(loritta: LorittaBot, userId: Long, function: suspend (MessageReactionAddEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(null, this.channel.idLong, userId) }
	functions.onReactionAddByAuthor = function
	return this
}

/**
 * When the command executor removes a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionRemoveByAuthor(context: CommandContext, function: suspend (MessageReactionRemoveEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = context.loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onReactionRemoveByAuthor = function
	return this
}

/**
 * When an user sends a message on the same text channel as the executed command
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onResponse(context: CommandContext, function: suspend (LorittaMessageEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = context.loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onResponse = function
	return this
}

/**
 * When the command executor sends a message on the same text channel as the executed command
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onResponseByAuthor(context: CommandContext, function: suspend (LorittaMessageEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = context.loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onResponseByAuthor = function
	return this
}

/**
 * When a message is received in any guild
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onMessageReceived(context: CommandContext, function: suspend (LorittaMessageEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = context.loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.userHandle.idLong) }
	functions.onMessageReceived = function
	return this
}

/**
 * When the command executor adds a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionAddByAuthor(context: DiscordCommandContext, function: suspend (MessageReactionAddEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong
	return onReactionAddByAuthor(context.loritta, context.user.idLong, guildId, channelId, function)
}

/**
 * When the command executor adds a reaction to this message
 *
 * @param userId    the user's ID
 * @param guildId   the guild's ID, may be null
 * @param channelId the channel's ID, may be null
 * @return          the message object for chaining
 */
fun Message.onReactionAddByAuthor(loritta: LorittaBot, userId: Long, guildId: Long?, channelId: Long?, function: suspend (MessageReactionAddEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, userId) }
	functions.onReactionAddByAuthor = function
	return this
}

/**
 * When the command executor adds or removes a reaction to this message
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionByAuthor(context: DiscordCommandContext, function: suspend (GenericMessageReactionEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	onReactionByAuthor(context.loritta, context.user.idLong, guildId, channelId, function)
	return this
}

/**
 * When the command executor adds or removes a reaction to this message
 *
 * @param userId    the user's ID
 * @param guildId   the guild's ID, may be null
 * @param channelId the channel's ID, may be null
 * @param function  the callback that should be invoked
 * @return          the message object for chaining
 */
fun Message.onReactionByAuthor(loritta: LorittaBot, userId: Long, guildId: Long?, channelId: Long?, function: suspend (GenericMessageReactionEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, userId) }
	functions.onReactionByAuthor = function
	return this
}

/**
 * When the command executor sends a message on the same text channel as the executed command
 *
 * @param userId    the user's ID
 * @param guildId   the guild's ID, may be null
 * @param channelId the channel's ID, may be null
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onResponseByAuthor(loritta: LorittaBot, userId: Long, guildId: Long?, channelId: Long?, function: suspend (LorittaMessageEvent) -> Unit): Message {
	val functions = loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, userId) }
	functions.onResponseByAuthor = function
	return this
}

/**
 * When the command executor sends a message on the same text channel as the executed command
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onResponseByAuthor(context: DiscordCommandContext, function: suspend (LorittaMessageEvent) -> Unit): Message {
	val guildId = if (this.isFromType(ChannelType.PRIVATE)) null else this.guild.idLong
	val channelId = if (this.isFromType(ChannelType.PRIVATE)) null else this.channel.idLong

	val functions = context.loritta.messageInteractionCache.getOrPut(this.idLong) { MessageInteractionFunctions(guildId, channelId, context.user.idLong) }
	functions.onResponseByAuthor = function
	return this
}

/**
 * Removes all interaction functions associated with [this]
 */
fun Message.removeAllFunctions(loritta: LorittaBot): Message {
	loritta.messageInteractionCache.remove(this.idLong)
	return this
}

class MessageInteractionFunctions(val guildId: Long?, val channelId: Long?, val originalAuthor: Long) {
	// Caso guild == null, quer dizer que foi uma mensagem recebida via DM!
	var onReactionAdd: (suspend (MessageReactionAddEvent) -> Unit)? = null
	var onReactionRemove: (suspend (MessageReactionRemoveEvent) -> Unit)? = null
	var onReactionAddByAuthor: (suspend (MessageReactionAddEvent) -> Unit)? = null
	var onReactionRemoveByAuthor: (suspend (MessageReactionRemoveEvent) -> Unit)? = null
	var onReactionByAuthor: (suspend (GenericMessageReactionEvent) -> Unit)? = null
	var onResponse: (suspend (LorittaMessageEvent) -> Unit)? = null
	var onResponseByAuthor: (suspend (LorittaMessageEvent) -> Unit)? = null
	var onMessageReceived: (suspend (LorittaMessageEvent) -> Unit)? = null
}