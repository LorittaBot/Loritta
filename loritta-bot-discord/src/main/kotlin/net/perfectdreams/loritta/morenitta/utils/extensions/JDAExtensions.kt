package net.perfectdreams.loritta.morenitta.utils.extensions

import kotlinx.coroutines.future.await
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.Permission.*
import net.dv8tion.jda.api.entities.*
import net.dv8tion.jda.api.entities.Message.MentionType
import net.dv8tion.jda.api.entities.channel.ChannelType
import net.dv8tion.jda.api.entities.channel.middleman.GuildChannel
import net.dv8tion.jda.api.entities.channel.middleman.GuildMessageChannel
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel
import net.dv8tion.jda.api.entities.channel.unions.MessageChannelUnion
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.entities.emoji.EmojiUnion
import net.dv8tion.jda.api.exceptions.ErrorResponseException
import net.dv8tion.jda.api.interactions.InteractionContextType
import net.dv8tion.jda.api.requests.ErrorResponse
import net.dv8tion.jda.api.requests.RestAction
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction
import net.dv8tion.jda.api.utils.messages.MessageCreateBuilder
import net.dv8tion.jda.api.utils.messages.MessageCreateData
import net.dv8tion.jda.api.utils.messages.MessageEditData
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.emotes.DiscordEmote
import net.perfectdreams.loritta.common.emotes.Emote
import net.perfectdreams.loritta.common.emotes.UnicodeEmote
import net.perfectdreams.loritta.i18n.I18nKeys
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ServerConfig
import net.perfectdreams.loritta.morenitta.utils.ImageFormat
import net.perfectdreams.loritta.morenitta.utils.stripCodeMarks
import net.perfectdreams.loritta.morenitta.utils.stripNewLines

suspend fun <T> RestAction<T>.await() : T = this.submit().await()

suspend fun MessageChannel.sendMessageAsync(text: String) = this.sendMessage(text).await()
suspend fun MessageChannel.sendMessageAsync(message: MessageCreateData) = this.sendMessage(message).await()
suspend fun MessageChannel.sendMessageAsync(embed: MessageEmbed) = this.sendMessageEmbeds(embed).await()

suspend fun Message.edit(message: String, embed: MessageEmbed, clearReactions: Boolean = true): Message {
    return this.edit(MessageCreateBuilder().setEmbeds(embed).setContent(if (message.isEmpty()) " " else message).build(), clearReactions)
}

suspend fun Message.edit(content: MessageCreateData, clearReactions: Boolean = true): Message {
    if (this.isFromType(ChannelType.PRIVATE) || !this.guild.selfMember.hasPermission(this.textChannel, Permission.MESSAGE_MANAGE)) {
        // Nós não podemos limpar as reações das mensagens caso a gente esteja em uma DM ou se a Lori não tem permissão para gerenciar mensagens
        // Nestes casos, iremos apenas deletar a mensagem e reenviar
        this.delete().queue()
        return this.channel.sendMessage(content).await()
    }

    // Se não, vamos apagar as reações e editar a mensagem atual!
    if (clearReactions)
        this.clearReactions().await()
    return this.editMessage(MessageEditData.fromCreateData(content)).await()
}

suspend fun MessageHistory.retrievePastChunked(quantity: Int): List<Message> {
    val messages = mutableListOf<Message>()

    for (x in 0 until quantity step 100) {
        val newMessages = this.retrievePast(100).await()
        if (newMessages.isEmpty())
            break

        messages += newMessages
    }
    return messages
}

suspend fun MessageHistory.retrieveAllMessages(): List<Message> {
    val messages = mutableListOf<Message>()

    while (true) {
        val newMessages = this.retrievePast(100).await()
        if (newMessages.isEmpty())
            break

        messages += newMessages
    }

    return messages
}

suspend fun Guild.retrieveMemberOrNullById(id: String) = retrieveMemberOrNullById(id.toLong())

/**
 * Retrieves a member, if the member isn't in the guild then null is returned
 *
 * @param the member's id
 * @return the member or null
 */
suspend fun Guild.retrieveMemberOrNullById(id: Long): Member? {
    return try {
        this.retrieveMemberById(id).await()
    } catch (e: ErrorResponseException) {
        if (e.errorResponse == ErrorResponse.UNKNOWN_MEMBER || e.errorResponse == ErrorResponse.UNKNOWN_USER)
            return null
        throw e
    }
}

/**
 * Retrieves a member, if the member isn't in the guild then null is returned
 *
 * @param the member's id
 * @return the member or null
 */
suspend fun Guild.retrieveMemberOrNull(user: User): Member? {
    return try {
        this.retrieveMember(user).await()
    } catch (e: ErrorResponseException) {
        if (e.errorResponse == ErrorResponse.UNKNOWN_MEMBER || e.errorResponse == ErrorResponse.UNKNOWN_USER)
            return null
        throw e
    }
}

/**
 * Edits the message, but only if the content was changed
 *
 * This reduces the number of API requests needed
 */
suspend fun Message.editMessageIfContentWasChanged(message: String): Message {
    if (this.contentRaw == message)
        return this

    return this.editMessage(message).await()
}

/**
 * Adds the [emotes] to the [message] if needed, this avoids a lot of unnecessary API requests
 */
suspend fun Message.doReactions(vararg emotes: String): Message {
    var message = this

    var clearAll = false

    // Vamos pegar todas as reações que não deveriam estar aqui

    val emoteOnlyIds = emotes.map { str -> str.split(":").getOrNull(1) }.filterNotNull()

    val invalidReactions = this.reactions.filterNot {
        if (it.emoji.type == Emoji.Type.CUSTOM)
            emoteOnlyIds.contains(it.emoji.asCustom().id)
        else
            emotes.contains(it.emoji.name)
    }

    if (invalidReactions.isNotEmpty())
        clearAll = true

    // Se o número de reações for diferente das reações na mensagem, então algo está errado ;w;
    if (this.reactions.size != emotes.size)
        clearAll = true

    if (clearAll) { // Pelo visto tem alguns emojis que não deveriam estar aqui, vamos limpar!
        this.clearReactions().await() // Vamos limpar todas as reações
        message = this.refresh().await() // E pegar o novo obj da mensagem

        emotes.forEach {
            // E agora vamos readicionar os emotes!
            message.addReaction(it).await()
        }
    }
    return message
}

/**
 * Hacky workaround for JDA v4 (and JDA v5 lol) support
 */
fun EmojiUnion.isEmote(id: String): Boolean {
    return if (this.type == Emoji.Type.CUSTOM)
        this.asCustom().id == id || this.asCustom().name == id
    else
        this.name == id
}

/**
 * Hacky workaround for JDA v5 support
 */
val MessageChannelUnion.textChannel
    get() = this.asTextChannel()

/**
 * Hacky workaround for JDA v5 support
 */
val Message.textChannel
    get() = this.channel.textChannel

/**
 * Hacky workaround for JDA v5 support
 */
fun MessageCreateBuilder.denyMentions(vararg mentions: MentionType): MessageCreateBuilder {
    this.setAllowedMentions(
        MentionType.values().toMutableSet().apply {
            this.removeAll(mentions)
        }
    )
    return this
}

/**
 * Hacky workaround for JDA v5 support
 */
fun Message.addReaction(reaction: String): RestAction<Void> {
    val emoji = if (reaction.contains(":"))
        Emoji.fromFormatted(reaction)
    else Emoji.fromUnicode(reaction)

    return this.addReaction(emoji)
}

fun Message.refresh(): RestAction<Message> {
    return this.channel.retrieveMessageById(this.idLong)
}

/**
 * Checks if a role is a valid giveable role (not managed, not a public role, etc).
 *
 * @return       if the role can be given to the specified member
 */
fun Role.canBeGiven() = !this.isPublicRole &&
        !this.isManaged &&
        guild.selfMember.canInteract(this)

/**
 * Filters a role list with [canBeGiven].
 *
 * @param member the member that the role will be given to
 * @return       all roles that can be given to the member
 */
fun Collection<Role>.filterOnlyGiveableRoles() = this.filter { it.canBeGiven() }

/**
 * Filters a role list with [canBeGiven].
 *
 * @param member the member that the role will be given to
 * @return       all roles that can be given to the member
 */
fun Sequence<Role>.filterOnlyGiveableRoles() = this.filter { it.canBeGiven() }

/**
 * Tries to send [targetMessagesPerSecond] messages every second.
 *
 * Discord has a 50 messages every 10s global rate limit (10 messages per second) and, sometimes,
 * we need to queue messages to be sent.
 *
 * This will try to queue all messages to fit in the [targetMessagesPerSecond] messages per second, avoiding
 * getting globally rate limited by Discord.
 *
 * @param sentMessages            how many messages were sent
 * @param targetMessagesPerSecond what is the message per second target
 */
fun RestAction<Message>.queueAfterWithMessagePerSecondTarget(
    sentMessages: Int,
    targetMessagesPerSecond: Int = 5
) {
    // Technically we can send 50 messages every 10s (so 10 messages per second)
    // To avoid getting global ratelimited to heck (and dying!), we need to have some delays to avoid that.
    //
    // So, let's reserve 5 of the total 10 messages to sending notification updates.
    // This should avoid spamming the API with requests.
    this.queueAfter(
        (sentMessages / targetMessagesPerSecond).toLong(),
        java.util.concurrent.TimeUnit.SECONDS
    )
}

/**
 * Tries to send [targetMessagesPerSecond] messages every second.
 *
 * Discord has a 50 messages every 10s global rate limit (10 messages per second) and, sometimes,
 * we need to queue messages to be sent.
 *
 * This will try to queue all messages to fit in the [targetMessagesPerSecond] messages per second, avoiding
 * getting globally rate limited by Discord.
 *
 * This also tries to load balance between all clusters, useful for multi cluster notifications.
 *
 * @param sentMessages            how many messages were sent
 * @param targetMessagesPerSecond what is the message per second target
 */
fun RestAction<Message>.queueAfterWithMessagePerSecondTargetAndClusterLoadBalancing(
    loritta: LorittaBot,
    sentMessages: Int,
    targetMessagesPerSecond: Int = 5
) {
    // Technically we can send 50 messages every 10s (so 10 messages per second)
    // To avoid getting global ratelimited to heck (and dying!), we need to have some delays to avoid that.
    //
    // Because we have multiple clusters, we need to split up the load depending on how many clusters
    // Loritta has. The target messages per second will be (target - how many clusters), minimum value is 1
    //
    // So, let's reserve (5 - how many clusters we have) of the total 10 messages to sending notification updates.
    // This should avoid spamming the API with requests.
    this.queueAfter(
        sentMessages / Math.max(1, targetMessagesPerSecond - loritta.config.loritta.clusters.instances.size.toLong()),
        java.util.concurrent.TimeUnit.SECONDS
    )
}

/**
 * Make the message a reply to the referenced message.
 *
 * This checks if the bot has [net.dv8tion.jda.api.Permission.MESSAGE_HISTORY] and, if it has, the message is referenced.
 *
 * @param message The target message
 *
 * @return Updated MessageAction for chaining convenience
 */
fun MessageCreateAction.referenceIfPossible(message: Message): MessageCreateAction {
    if (message.isFromGuild && !message.guild.selfMember.hasPermission(message.channel as GuildChannel, MESSAGE_HISTORY))
        return this
    return this.setMessageReference(message)
}

/**
 * Make the message a reply to the referenced message.
 *
 * This has the same checks as [referenceIfPossible] plus a check to see if [addInlineReply] is enabled and to check if [ServerConfig.deleteMessageAfterCommand] is false.
 *
 * @param message The target message
 *
 * @return Updated MessageAction for chaining convenience
 */
fun MessageCreateAction.referenceIfPossible(message: Message, serverConfig: ServerConfig, addInlineReply: Boolean = true): MessageCreateAction {
    // We check if deleteMessageAfterCommand is true because it doesn't matter trying to reply to a message that's going to be deleted.
    if (!addInlineReply || serverConfig.deleteMessageAfterCommand)
        return this
    return this.referenceIfPossible(message)
}

fun Guild.getGuildMessageChannelByName(channelName: String, ignoreCase: Boolean) = this.channels
    .asSequence()
    .filterIsInstance<GuildMessageChannel>().filter { it.name.equals(channelName, ignoreCase) }
    .firstOrNull()

fun Guild.getGuildMessageChannelById(channelId: String) = getGuildMessageChannelById(channelId.toLong())

fun Guild.getGuildMessageChannelById(channelId: Long) = this.getGuildChannelById(channelId) as? GuildMessageChannel

fun Permission.getI18nKey() = when (this) {
    CREATE_INSTANT_INVITE -> I18nKeys.Permissions.CreateInstantInvite
    KICK_MEMBERS -> I18nKeys.Permissions.KickMembers
    BAN_MEMBERS -> I18nKeys.Permissions.BanMembers
    ADMINISTRATOR -> I18nKeys.Permissions.Administrator
    MANAGE_CHANNEL -> I18nKeys.Permissions.ManageChannels
    MANAGE_SERVER -> I18nKeys.Permissions.ManageGuild
    MESSAGE_ADD_REACTION -> I18nKeys.Permissions.AddReactions
    VIEW_AUDIT_LOGS -> I18nKeys.Permissions.ViewAuditLog
    VIEW_CHANNEL -> I18nKeys.Permissions.ViewChannel
    MESSAGE_SEND -> I18nKeys.Permissions.SendMessages
    MESSAGE_TTS -> I18nKeys.Permissions.SendTTSMessages
    MESSAGE_MANAGE -> I18nKeys.Permissions.ManageMessages
    MESSAGE_EMBED_LINKS -> I18nKeys.Permissions.EmbedLinks
    MESSAGE_ATTACH_FILES -> I18nKeys.Permissions.AttachFiles
    MESSAGE_HISTORY -> I18nKeys.Permissions.ReadMessageHistory
    MESSAGE_MENTION_EVERYONE -> I18nKeys.Permissions.MentionEveryone
    MESSAGE_EXT_EMOJI -> I18nKeys.Permissions.UseExternalEmojis
    VIEW_GUILD_INSIGHTS -> I18nKeys.Permissions.ViewGuildInsights
    VOICE_CONNECT -> I18nKeys.Permissions.Connect
    VOICE_SPEAK -> I18nKeys.Permissions.Speak
    VOICE_MUTE_OTHERS -> I18nKeys.Permissions.MuteMembers
    VOICE_DEAF_OTHERS -> I18nKeys.Permissions.DeafenMembers
    VOICE_MOVE_OTHERS -> I18nKeys.Permissions.MoveMembers
    VOICE_USE_VAD -> I18nKeys.Permissions.UseVAD
    PRIORITY_SPEAKER -> I18nKeys.Permissions.PrioritySpeaker
    NICKNAME_CHANGE -> I18nKeys.Permissions.ChangeNickname
    NICKNAME_MANAGE -> I18nKeys.Permissions.ManageNicknames
    MANAGE_ROLES -> I18nKeys.Permissions.ManageRoles
    MANAGE_WEBHOOKS -> I18nKeys.Permissions.ManageWebhooks
    MANAGE_GUILD_EXPRESSIONS -> I18nKeys.Permissions.ManageEmojisAndStickers
    USE_APPLICATION_COMMANDS -> I18nKeys.Permissions.UseApplicationCommands
    REQUEST_TO_SPEAK -> I18nKeys.Permissions.RequestToSpeak
    MANAGE_THREADS -> I18nKeys.Permissions.ManageThreads
    CREATE_PUBLIC_THREADS -> I18nKeys.Permissions.CreatePublicThreads
    CREATE_PRIVATE_THREADS -> I18nKeys.Permissions.CreatePrivateThreads
    MESSAGE_SEND_IN_THREADS -> I18nKeys.Permissions.SendMessagesInThreads
    MANAGE_PERMISSIONS -> I18nKeys.Permissions.ManagePermissions
    MANAGE_EVENTS -> I18nKeys.Permissions.ManageEvents
    MODERATE_MEMBERS -> I18nKeys.Permissions.ModerateMembers
    MESSAGE_EXT_STICKER -> I18nKeys.Permissions.UseExternalStickers
    VOICE_STREAM -> I18nKeys.Permissions.Video
    VOICE_START_ACTIVITIES -> I18nKeys.Permissions.StartActivities
    MANAGE_GUILD_EXPRESSIONS -> I18nKeys.Permissions.ManageGuildExpressions
    VIEW_CREATOR_MONETIZATION_ANALYTICS -> I18nKeys.Permissions.ViewCreatorMonetizationAnalytics
    MESSAGE_ATTACH_VOICE_MESSAGE -> I18nKeys.Permissions.MessageAttachVoiceMessage
    VOICE_USE_SOUNDBOARD -> I18nKeys.Permissions.VoiceUseSoundboard
    VOICE_USE_EXTERNAL_SOUNDS -> I18nKeys.Permissions.VoiceUseExternalSounds
    VOICE_SET_STATUS -> I18nKeys.Permissions.VoiceSetStatus
    USE_EMBEDDED_ACTIVITIES -> I18nKeys.Permissions.StartActivities
    CREATE_GUILD_EXPRESSIONS -> I18nKeys.Permissions.CreateGuildExpressions
    CREATE_SCHEDULED_EVENTS -> I18nKeys.Permissions.CreateScheduledEvents
    MESSAGE_SEND_POLLS -> I18nKeys.Permissions.MessageSendPolls
    USE_EXTERNAL_APPLICATIONS -> I18nKeys.Permissions.UseExternalApplications
    UNKNOWN -> I18nKeys.Permissions.UnknownPermission
}

fun Permission.getLocalizedName(i18nContext: I18nContext) = i18nContext.get(this.getI18nKey())

/**
 * Gets the effective avatar URL in the specified [format]
 *
 * @see getEffectiveAvatarUrl
 */
fun User.getEffectiveAvatarUrl(format: ImageFormat) = getEffectiveAvatarUrl(format, 128)

/**
 * Gets the effective avatar URL in the specified [format] and [ímageSize]
 *
 * @see getEffectiveAvatarUrlInFormat
 */
fun User.getEffectiveAvatarUrl(format: ImageFormat, imageSize: Int): String {
    val extension = format.extension

    return if (avatarId != null) {
        "https://cdn.discordapp.com/avatars/$id/$avatarId.${extension}?size=$imageSize"
    } else {
        val avatarId = idLong % 5
        // This only exists in png AND doesn't have any other sizes
        "https://cdn.discordapp.com/embed/avatars/$avatarId.png"
    }
}

/**
 * Gets the guild's icon URL in the specified [format] and [ímageSize]
 *
 * @see getEffectiveAvatarUrlInFormat
 */
fun Guild.getIconUrl(size: Int, format: ImageFormat): String? {
    val iconId = this.iconId ?: return null
    return String.format(Guild.ICON_URL, this.id, iconId, format.extension)
}

/**
 * Gets the [User]'s name within a markdown inline code block, with their name mention and ID.
 */
fun User.asUserNameCodeBlockPreviewTag(
    stripCodeMarksFromInput: Boolean = true,
    stripLinksFromInput: Boolean = true
): String {
    val globalName = this.globalName
    var globalPreviewName = globalName

    // We only remove code marks and links from global names because usernames cannot have codemarks or links
    // And because a lot of usernames have ".", this ends up borking and making the username blank, so we don't strip from those
    if (globalPreviewName != null) {
        if (stripCodeMarksFromInput)
            globalPreviewName = globalPreviewName.stripCodeMarks()
        if (stripLinksFromInput)
            globalPreviewName = globalPreviewName.stripLinks()
    }

    val hasPomelo = this.discriminator == "0000"

    // We DO remove if they don't have pomelo tho
    val previewName = globalPreviewName ?: if (hasPomelo) name else name.stripCodeMarks().stripLinks()

    val nameDisplay = if (hasPomelo) {
        "@$name"
    } else {
        "$previewName#${discriminator}"
    }

    return "${safeInlineCodeBlock(previewName)} (${safeInlineCodeBlock(nameDisplay)} | `$id`)"
}

/**
 * Gets the [User]'s global name in a `Global Name` format, or gets the [User]'s legacy (old username system) in a `Username#1234` format,
 * depending if the user has already migrated to the new username system or not.
 */
val User.asGlobalNameOrLegacyTag: String
    get() {
        val globalName = this.globalName
        val hasPomelo = this.discriminator == "0000"

        if (globalName != null)
            return globalName

        return if (hasPomelo)
            this.name
        else
            this.asTag
    }

/**
 * Gets the [User]'s pomelo (new username system) tag in a `@username` format, or gets the [User]'s legacy (old username system) in a `Username#1234` format,
 * depending if the user has already migrated to the new username system or not.
 */
val User.asPomeloOrLegacyTag
    get() = if (this.discriminator == "0000") { "@${this.name}" } else this.asTag

/**
 * Safely creates an inline code block with the following [input]
 *
 * Code marks (`) and new lines are stripped from the input, and if the content is blank, the result will be ` ` to avoid formatting issues
 */
fun safeInlineCodeBlock(input: String): String {
    return "`${input.stripNewLines().stripCodeMarks().ifBlank { " " }}`"
}

/**
 * Converts an [Emote] to a JDA [Emoji]
 */
fun Emote.toJDA() = when (this) {
    is DiscordEmote -> Emoji.fromCustom(
        this.name,
        this.id,
        this.animated
    )

    is UnicodeEmote -> Emoji.fromUnicode(this.name)
}

/**
 * Converts a [InteractionContextType] to [net.perfectdreams.loritta.common.commands.InteractionContextType]
 */
fun InteractionContextType.toLoritta(): net.perfectdreams.loritta.common.commands.InteractionContextType {
    return when (this) {
        InteractionContextType.UNKNOWN -> net.perfectdreams.loritta.common.commands.InteractionContextType.UNKNOWN
        InteractionContextType.GUILD -> net.perfectdreams.loritta.common.commands.InteractionContextType.GUILD
        InteractionContextType.BOT_DM -> net.perfectdreams.loritta.common.commands.InteractionContextType.BOT_DM
        InteractionContextType.PRIVATE_CHANNEL -> net.perfectdreams.loritta.common.commands.InteractionContextType.PRIVATE_CHANNEL
    }
}