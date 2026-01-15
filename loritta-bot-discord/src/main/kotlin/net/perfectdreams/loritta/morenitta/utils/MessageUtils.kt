package net.perfectdreams.loritta.morenitta.utils

import com.github.salomonbrys.kotson.*
import com.google.gson.JsonParser
import dev.minn.jda.ktx.messages.MessageCreate
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject
import net.perfectdreams.harmony.logging.HarmonyLoggerFactory
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.components.Component
import net.dv8tion.jda.api.components.actionrow.ActionRow
import net.dv8tion.jda.api.components.actionrow.ActionRowChildComponent
import net.dv8tion.jda.api.components.buttons.Button
import net.dv8tion.jda.api.components.buttons.ButtonStyle
import net.dv8tion.jda.api.components.section.Section
import net.dv8tion.jda.api.components.textdisplay.TextDisplay
import net.dv8tion.jda.api.components.thumbnail.Thumbnail
import net.dv8tion.jda.api.components.mediagallery.MediaGallery
import net.dv8tion.jda.api.components.mediagallery.MediaGalleryItem
import net.dv8tion.jda.api.components.separator.Separator
import net.dv8tion.jda.api.components.container.Container
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
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.*
import net.perfectdreams.loritta.common.utils.placeholders.*
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordComponent
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordEmbed
import net.perfectdreams.loritta.dashboard.discordmessages.DiscordMessage
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.commands.CommandContext
import net.perfectdreams.loritta.morenitta.events.LorittaMessageEvent
import net.perfectdreams.loritta.morenitta.interactions.UnleashedContext
import net.perfectdreams.loritta.morenitta.interactions.linkButton
import net.perfectdreams.loritta.morenitta.platform.discord.legacy.commands.DiscordCommandContext
import net.perfectdreams.loritta.morenitta.utils.MessageUtils.watermarkSayMessage
import net.perfectdreams.loritta.morenitta.utils.extensions.isValidUrl
import net.perfectdreams.loritta.morenitta.utils.placeholders.RenderableMessagePlaceholder

object MessageUtils {
    private val logger by HarmonyLoggerFactory.logger {}
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
        val tokensAsMap = mutableMapOf<String, String>()

        for (token in customTokens) {
            for (name in token.placeholder.names) {
                tokensAsMap[name.placeholder.name] = token.replaceWith
            }
        }

        return generateMessage(message, guild, tokensAsMap, safe)
    }

    fun generateMessage(
        message: String,
        guild: Guild?,
        customTokens: Map<String, String>,
        safe: Boolean
    ): MessageCreateData {
        val originalDiscordMessage = DiscordMessage.decodeFromJsonString(message) ?: DiscordMessage(content = message)

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

                is DiscordComponent.DiscordSection -> {
                    with(component) {
                        component.copy(
                            components = this.components.map { recursiveComponentReplacer(it) },
                            accessory = this.accessory?.let { recursiveComponentReplacer(it) }
                        )
                    }
                }

                is DiscordComponent.DiscordTextDisplay -> {
                    with(component) {
                        component.copy(
                            // Text displays can have up to 4000 chars total in V2
                            // For individual component, use a reasonable limit (e.g., 2000)
                            content = processStringAndReplaceTokens(this.content, 2000, guild, customTokens)
                        )
                    }
                }

                is DiscordComponent.DiscordThumbnail -> {
                    with(component) {
                        component.copy(
                            url = replaceTokens(this.url, guild, customTokens),
                            description = this.description?.let {
                                processStringAndReplaceTokens(it, 1024, guild, customTokens)
                            }
                        )
                    }
                }

                is DiscordComponent.DiscordMediaGallery -> {
                    with(component) {
                        component.copy(
                            items = this.items.map { item ->
                                item.copy(
                                    media = item.media.copy(replaceTokens(item.media.url, guild, customTokens)),
                                    description = item.description?.let {
                                        processStringAndReplaceTokens(it, 1024, guild, customTokens)
                                    }
                                )
                            }
                        )
                    }
                }

                is DiscordComponent.DiscordSeparator -> component

                is DiscordComponent.DiscordContainer -> {
                    with(component) {
                        component.copy(
                            components = this.components.map { recursiveComponentReplacer(it) }
                            // accentColor and spoiler don't need token replacement
                        )
                    }
                }
            }
        }

        // Check for Components V2 flag
        val isComponentsV2 = (originalDiscordMessage.flags ?: 0) and (1 shl 15) != 0

        // Process the message based on V2 flag
        val discordMessage = if (isComponentsV2) {
            // V2 mode: clear content/embeds, process all components
            with(originalDiscordMessage) {
                copy(
                    content = null, // Must be null in V2 mode
                    embeds = null, // Cannot have embeds in V2 mode
                    components = components?.map { recursiveComponentReplacer(it) }
                )
            }
        } else {
            // Legacy mode: process content/embeds normally
            with(originalDiscordMessage) {
                copy(
                    content = content?.let { processStringAndReplaceTokens(it, 2000, guild, customTokens) },
                    embeds = embeds?.map { processEmbed(it, guild, customTokens) },
                    components = components?.map { recursiveComponentReplacer(it) }
                )
            }
        }

        val messageBuilder = MessageCreateBuilder()

        // Enable V2 mode in JDA if flag is set
        if (isComponentsV2) {
            messageBuilder.useComponentsV2()
        } else {
            // Set content and embeds only in legacy mode
            messageBuilder.setContent(discordMessage.content)
            messageBuilder.setTTS(discordMessage.tts)
        }

        if (discordMessage.embeds != null) {
            for (discordEmbed in discordMessage.embeds) {
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
                    messageBuilder.addEmbeds(embed.build())
                } catch (e: Exception) {
                    // Creating a empty embed can cause errors, so we just wrap it in a try .. catch block and hope
                    // for the best!
                }
            }
        }

        // Component converter that handles both legacy and Components V2 types
        fun recursiveComponentConverter(component: DiscordComponent): Component {
            return when (component) {
                is DiscordComponent.DiscordActionRow -> {
                    // ActionRows cannot have other ActionRows within them so whatever
                    ActionRow.of(component.components.map { recursiveComponentConverter(it) as ActionRowChildComponent })
                }

                is DiscordComponent.DiscordButton -> {
                    // We ONLY support link buttons
                    Button.of(
                        ButtonStyle.LINK,
                        component.url,
                        component.label
                    )
                }

                is DiscordComponent.DiscordSection -> {
                    val accessory = component.accessory
                    if (accessory != null) {
                        // JDA requires non-null accessory for Section
                        // Cast components to proper types
                        val convertedAccessory = recursiveComponentConverter(accessory) as net.dv8tion.jda.api.components.section.SectionAccessoryComponent
                        val convertedContent = component.components.map {
                            recursiveComponentConverter(it) as net.dv8tion.jda.api.components.section.SectionContentComponent
                        }
                        Section.of(convertedAccessory, convertedContent)
                    } else {
                        error("Cannot have a section without an accessory!")
                    }
                }

                is DiscordComponent.DiscordTextDisplay -> {
                    TextDisplay.of(component.content)
                }

                is DiscordComponent.DiscordThumbnail -> {
                    var thumbnail = Thumbnail.fromUrl(component.url)

                    if (component.description != null) {
                        thumbnail = thumbnail.withDescription(component.description)
                    }

                    if (component.spoiler) {
                        thumbnail = thumbnail.withSpoiler(true)
                    }

                    thumbnail
                }

                is DiscordComponent.DiscordMediaGallery -> {
                    val items = component.items.map { item ->
                        var galleryItem = MediaGalleryItem.fromUrl(item.media.url)

                        if (item.description != null) {
                            galleryItem = galleryItem.withDescription(item.description)
                        }

                        if (item.spoiler) {
                            galleryItem = galleryItem.withSpoiler(true)
                        }

                        galleryItem
                    }

                    MediaGallery.of(items)
                }

                is DiscordComponent.DiscordSeparator -> {
                    val spacing = when (component.spacing) {
                        1 -> Separator.Spacing.SMALL
                        2 -> Separator.Spacing.LARGE
                        else -> Separator.Spacing.SMALL // Default fallback
                    }

                    if (component.divider) {
                        Separator.createDivider(spacing)
                    } else {
                        Separator.createInvisible(spacing)
                    }
                }

                is DiscordComponent.DiscordContainer -> {
                    // Convert child components - cast components to proper types
                    val childComponents = component.components.map {
                        recursiveComponentConverter(it) as net.dv8tion.jda.api.components.container.ContainerChildComponent
                    }

                    // Create container
                    var jdaContainer: Container = Container.of(childComponents)

                    // Apply accent color if specified
                    if (component.accentColor != null) {
                        jdaContainer = jdaContainer.withAccentColor(component.accentColor)
                    }

                    // Apply spoiler if specified
                    if (component.spoiler) {
                        jdaContainer = jdaContainer.withSpoiler(true)
                    }

                    jdaContainer
                }
            }
        }

        // Process components
        val components = discordMessage.components
        if (components != null) {
            for (component in components) {
                val convertedComponent = recursiveComponentConverter(component)

                if (isComponentsV2) {
                    // V2 mode: components can be top-level (not just ActionRow)
                    // Cast to MessageTopLevelComponent for V2
                    messageBuilder.addComponents(convertedComponent as net.dv8tion.jda.api.components.MessageTopLevelComponent)
                } else {
                    // Legacy mode: must be ActionRow
                    if (convertedComponent is ActionRow) {
                        messageBuilder.addComponents(convertedComponent)
                    }
                }
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
                        "https://dash.loritta.website/", // woo, hardcoded!
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

    private fun replaceTokens(text: String, guild: Guild?, customTokens: Map<String, String>): String {
        var message = text

        for ((key, value) in customTokens)
            message = message.replace("{$key}", value)

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

    private fun processEmbed(embed: DiscordEmbed, guild: Guild?, customTokens: Map<String, String>): DiscordEmbed {
        return with(embed) {
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
    }

    private fun replaceTokensIfNotNull(text: String?, guild: Guild?, customTokens: Map<String, String>) = text?.let { replaceTokens(text, guild, customTokens) }

    private fun processStringAndReplaceTokens(text: String, maxSize: Int, guild: Guild?, customTokens: Map<String, String>) = processString(replaceTokens(text, guild, customTokens), maxSize)

    private fun processStringAndReplaceTokensIfNotNull(text: String?, maxSize: Int, guild: Guild?, customTokens: Map<String, String>) = text?.let { processString(replaceTokens(it, guild, customTokens), maxSize) }

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
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onReactionAddByAuthor(context: UnleashedContext, function: suspend (MessageReactionAddEvent) -> Unit): Message {
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
 * When the command executor sends a message on the same text channel as the executed command
 *
 * @param context  the context of the message
 * @param function the callback that should be invoked
 * @return         the message object for chaining
 */
fun Message.onResponseByAuthor(context: UnleashedContext, function: suspend (LorittaMessageEvent) -> Unit): Message {
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