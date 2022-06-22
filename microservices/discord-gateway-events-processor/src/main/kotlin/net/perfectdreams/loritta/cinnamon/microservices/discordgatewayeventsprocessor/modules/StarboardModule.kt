package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules

import com.github.benmanes.caffeine.cache.Caffeine
import dev.kord.common.Color
import dev.kord.common.entity.DiscordMessage
import dev.kord.common.entity.MessageStickerType
import dev.kord.common.entity.Reaction
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Event
import dev.kord.gateway.MessageReactionAdd
import dev.kord.gateway.MessageReactionRemove
import dev.kord.gateway.MessageReactionRemoveAll
import dev.kord.gateway.MessageReactionRemoveEmoji
import dev.kord.rest.builder.message.EmbedBuilder
import dev.kord.rest.builder.message.create.UserMessageCreateBuilder
import dev.kord.rest.builder.message.create.actionRow
import dev.kord.rest.builder.message.create.embed
import dev.kord.rest.builder.message.modify.UserMessageModifyBuilder
import dev.kord.rest.builder.message.modify.actionRow
import dev.kord.rest.builder.message.modify.embed
import dev.kord.rest.request.KtorRequestException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.discordinteraktions.common.utils.author
import net.perfectdreams.discordinteraktions.common.utils.field
import net.perfectdreams.discordinteraktions.common.utils.thumbnailUrl
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.common.emotes.Emotes
import net.perfectdreams.loritta.cinnamon.common.utils.text.TextUtils.shortenWithEllipsis
import net.perfectdreams.loritta.cinnamon.i18n.I18nKeysData
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.DiscordGatewayEventsProcessor
import net.perfectdreams.loritta.cinnamon.platform.utils.ContentTypeUtils
import net.perfectdreams.loritta.cinnamon.platform.utils.UserUtils
import net.perfectdreams.loritta.cinnamon.pudding.tables.StarboardMessages
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import java.util.*
import java.util.concurrent.TimeUnit

class StarboardModule(private val m: DiscordGatewayEventsProcessor) : ProcessDiscordEventsModule() {
    companion object {
        const val STAR_REACTION = "⭐"
        private val logger = KotlinLogging.logger {}
    }

    // TODO: This doesn't scale, so we would need to move this data to the database later.
    private val failedToSendMessageChannels = Collections.newSetFromMap(
        Caffeine.newBuilder()
            .expireAfterWrite(60, TimeUnit.SECONDS)
            .build<Snowflake, Boolean>()
            .asMap()
    )
    private val mutexes = Caffeine.newBuilder()
        .expireAfterAccess(60, TimeUnit.SECONDS)
        .build<Long, Mutex>()
        .asMap()

    override suspend fun processEvent(event: Event) {
        when (event) {
            // ===[ REACTIONS ]===
            is MessageReactionAdd -> {
                handleStarboardReaction(
                    event.reaction.guildId.value ?: return,
                    event.reaction.channelId,
                    event.reaction.messageId,
                    event.reaction.emoji.name
                )
            }
            is MessageReactionRemove -> {
                handleStarboardReaction(
                    event.reaction.guildId.value ?: return,
                    event.reaction.channelId,
                    event.reaction.messageId,
                    event.reaction.emoji.name
                )
            }
            is MessageReactionRemoveEmoji -> {
                handleStarboardReaction(
                    event.reaction.guildId,
                    event.reaction.channelId,
                    event.reaction.messageId,
                    event.reaction.emoji.name
                )
            }
            is MessageReactionRemoveAll -> {
                handleStarboardReaction(
                    event.reactions.guildId.value ?: return,
                    event.reactions.channelId,
                    event.reactions.messageId,
                    STAR_REACTION // We only want the code to check if it should be removed from the starboard
                )
            }
            else -> {}
        }
    }

    suspend fun handleStarboardReaction(
        guildId: Snowflake,
        channelId: Snowflake,
        messageId: Snowflake,
        emojiName: String?
    ) {
        // If it isn't a star, return
        if (emojiName != STAR_REACTION)
            return

        val lockKey = UUID.nameUUIDFromBytes("starboard-message:$guildId:$channelId:$messageId".toByteArray(Charsets.UTF_8))
            .mostSignificantBits

        logger.info { "Creating lock $lockKey for Starboard Message ${messageId}..." }

        // Before we were using PostgreSQL's advisory locks, however for some reason all connections got exausted after a while (maybe a connection leak? however there weren't any active transactions in the database)? Super weird issue
        // So for now we are relying on Kotlin Coroutines' mutexes, the issue with this is that it won't scale if we increase the number of replicas
        mutexes.getOrPut(lockKey) { Mutex() }.withLock {
            // Because we need to do REST queries here, we may get rate limited by Discord!
            // That's why we use a different transaction pool, to avoid blocking issues.
            m.servicesWithHttpRequests.transaction {
                val serverConfig = m.services.serverConfigs._getServerConfigRoot(guildId.value) ?: return@transaction
                val starboardConfig = serverConfig._getStarboardConfig() ?: return@transaction
                val i18nContext = m.languageManager.getI18nContextByLegacyLocaleId(serverConfig.localeId)

                val starboardId = starboardConfig.starboardChannelId
                // Ignore if someone is trying to be "haha i'm so funni" trying to add stars to the starboard channel
                if (starboardId == channelId.value)
                    return@transaction

                if (Snowflake(starboardConfig.starboardChannelId) in failedToSendMessageChannels)
                    return@transaction

                logger.info { "Getting Starboard Message of $messageId in the database..." }

                // Get if the current message is already sent in the starboard
                val starboardMessageFromDatabase = StarboardMessages.selectFirstOrNull {
                    StarboardMessages.guildId eq guildId.value.toLong() and (StarboardMessages.messageId eq messageId.value.toLong())
                }

                logger.info { "Does message $messageId have a message in the database? ${starboardMessageFromDatabase != null}" }

                val reactedMessage = try {
                    m.rest.channel.getMessage(channelId, messageId)
                } catch (e: KtorRequestException) {
                    logger.warn(e) { "Failed to get message $messageId" }
                    failedToSendMessageChannels.add(channelId)
                    return@transaction
                }

                // Maybe null if there isn't any reactions in the message
                val reactionList = reactedMessage.reactions.value

                // The star may be missing if there isn't any reactions in the message
                val starReaction = reactionList?.firstOrNull { it.emoji.name == STAR_REACTION }

                val starReactionCount = starReaction?.count ?: 0

                logger.info { "Current Star Reaction Count for message $messageId: $starReactionCount" }

                if (starboardMessageFromDatabase != null && starboardConfig.requiredStars > starReactionCount) {
                    logger.info { "Starboard message for $messageId is present in the database, but it is below the required stars threshold! Deleting message..." }

                    // Star Reaction is less than the required star count, delete the starboard message and from the database
                    StarboardMessages.deleteWhere {
                        StarboardMessages.id eq starboardMessageFromDatabase[StarboardMessages.id]
                    }

                    // Bye starboard message!
                    try {
                        m.rest.channel.deleteMessage(
                            Snowflake(starboardId),
                            Snowflake(starboardMessageFromDatabase[StarboardMessages.embedId])
                        )
                    } catch (e: KtorRequestException) {
                        logger.warn(e) { "Failed to delete message ${starboardMessageFromDatabase[StarboardMessages.embedId]}" }
                    }
                    return@transaction
                }

                if (starReaction != null) {
                    if (starboardMessageFromDatabase != null) {
                        logger.info { "Starboard message for $messageId is present in the database!" }

                        val embedId = starboardMessageFromDatabase[StarboardMessages.embedId]

                        try {
                            m.rest.channel.editMessage(
                                Snowflake(starboardId),
                                Snowflake(embedId),
                                modifyStarboardMessage(
                                    i18nContext,
                                    guildId,
                                    reactedMessage,
                                    starReaction
                                )
                            )
                        } catch (e: KtorRequestException) {
                            logger.warn(e) { "Failed to edit starboard message $embedId in channel ${starboardConfig.starboardChannelId}" }
                            failedToSendMessageChannels.add(Snowflake(starboardId))
                            return@transaction
                        }
                    } else if (starReactionCount >= starboardConfig.requiredStars) {
                        logger.info { "Starboard message for $messageId is not present in the database..." }

                        // Message doesn't exist on the database, and we have enough stars to create the message! Create and send...

                        // Don't send messages to the starboard if the source channel is NSFW
                        // TODO: Caching
                        val channel = m.rest.channel.getChannel(channelId)
                        if (channel.nsfw.discordBoolean)
                            return@transaction

                        val newMessage = try {
                            m.rest.channel.createMessage(
                                Snowflake(starboardId),
                                createStarboardMessage(
                                    i18nContext,
                                    guildId,
                                    reactedMessage,
                                    starReaction
                                )
                            )
                        } catch (e: KtorRequestException) {
                            logger.warn(e) { "Failed to send starboard message in channel ${starboardConfig.starboardChannelId}" }
                            failedToSendMessageChannels.add(Snowflake(starboardId))
                            return@transaction
                        }

                        StarboardMessages.insert {
                            it[StarboardMessages.guildId] = guildId.value.toLong()
                            it[StarboardMessages.messageId] = reactedMessage.id.value.toLong()
                            it[StarboardMessages.embedId] = newMessage.id.value.toLong()
                        }
                    }
                }
            }
        }
    }

    private fun createStarboardMessage(
        i18nContext: I18nContext,
        guildId: Snowflake,
        message: DiscordMessage,
        reaction: Reaction
    ): UserMessageCreateBuilder.() -> (Unit) = {
        val emoji = getStarEmojiForReactionCount(reaction.count)

        content = "$emoji **${reaction.count}** - <#${message.channelId}>"

        val embed = createStarboardEmbed(i18nContext, message, reaction)

        embed(embed)

        actionRow {
            linkButton("https://discord.com/channels/$guildId/${message.channelId}/${message.id}") {
                label = i18nContext.get(I18nKeysData.Modules.Starboard.JumpToMessage)
            }
        }
    }

    private fun modifyStarboardMessage(
        i18nContext: I18nContext,
        guildId: Snowflake,
        message: DiscordMessage,
        reaction: Reaction
    ): UserMessageModifyBuilder.() -> (Unit) = {
        val emoji = getStarEmojiForReactionCount(reaction.count)

        content = "$emoji **${reaction.count}** - <#${message.channelId}>"

        val embed = createStarboardEmbed(i18nContext, message, reaction)

        embed(embed)

        actionRow {
            linkButton("https://discord.com/channels/$guildId/${message.channelId}/${message.id}") {
                label = i18nContext.get(I18nKeysData.Modules.Starboard.JumpToMessage)
            }
        }
    }

    private fun createStarboardEmbed(
        i18nContext: I18nContext,
        message: DiscordMessage,
        reaction: Reaction
    ): EmbedBuilder.() -> (Unit) = {
        val count = reaction.count

        author(
            message.author.username + "#" + message.author.discriminator + " (${message.author.id})",
            null,
            UserUtils.createUserAvatarOrDefaultUserAvatar(
                message.author.id,
                message.author.avatar,
                message.author.discriminator
            ).cdnUrl.toUrl()
        )

        // Show the message's attachments in the embed
        if (message.attachments.isNotEmpty()) {
            field(
                "${Emotes.FileFolder} ${i18nContext.get(I18nKeysData.Modules.Starboard.Files(message.attachments.size))}",
                message.attachments.joinToString("\n") {
                    "[${it.filename}](${it.url})"
                }
            )
        }

        // Cut if the message is too long
        description = message.content.shortenWithEllipsis(2048)

        // Set the embed's image to the first attachment in the message
        image = message.attachments.firstOrNull { it.contentType.value in ContentTypeUtils.COMMON_IMAGE_CONTENT_TYPES }?.url

        thumbnailUrl = message.stickers.value?.firstOrNull { it.formatType == MessageStickerType.PNG || it.formatType == MessageStickerType.APNG }?.let {
            // TODO: Move this to Discord InteraKTions' Icon class
            "https://cdn.discordapp.com/stickers/${it.id}.png"
        }

        color = Color(255, 255, (255 - (count * 15)).coerceAtLeast(0))
        timestamp = message.timestamp
    }

    private fun getStarEmojiForReactionCount(count: Int) = when {
        count == 69 -> Emotes.LoriBonk.toString() // Easter Egg
        count >= 20 -> "\uD83C\uDF0C"
        count >= 15 -> "\uD83D\uDCAB"
        count >= 10 -> "\uD83C\uDF20"
        count >= 5 -> "\uD83C\uDF1F"
        else -> STAR_REACTION
    }
}