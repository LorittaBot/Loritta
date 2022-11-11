package net.perfectdreams.loritta.deviousfun.events

import dev.kord.gateway.*
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.cache.DeviousCacheManager
import net.perfectdreams.loritta.deviousfun.cache.DeviousMessageFragmentData
import net.perfectdreams.loritta.deviousfun.entities.*
import net.perfectdreams.loritta.deviousfun.events.guild.GuildJoinEvent
import net.perfectdreams.loritta.deviousfun.events.guild.GuildLeaveEvent
import net.perfectdreams.loritta.deviousfun.events.message.create.MessageReceivedEvent
import net.perfectdreams.loritta.deviousfun.events.message.delete.MessageBulkDeleteEvent
import net.perfectdreams.loritta.deviousfun.events.message.delete.MessageDeleteEvent
import net.perfectdreams.loritta.deviousfun.events.message.react.MessageReactionAddEvent
import net.perfectdreams.loritta.deviousfun.events.message.react.MessageReactionRemoveEvent
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway
import net.perfectdreams.loritta.deviousfun.utils.DeviousUserUtils

/**
 * Creates Devious events based off Kord's gateway events
 */
class DeviousEventFactory(val m: DeviousShard) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun createMessageReceived(gateway: DeviousGateway, event: MessageCreate): MessageReceivedEvent? {
        val isWebhook = DeviousUserUtils.isSenderWebhookOrSpecial(event.message)
        val guildId = event.message.guildId.value

        val channel = m.getChannelById(event.message.channelId)
        if (channel == null) {
            logger.warn { "Received message received for a channel that we don't have in our cache! Guild ID: $guildId; Channel ID: ${event.message.channelId}" }
            return null
        }
        val guildResult = m.getCacheManager().getGuildFailIfSnowflakeIsNotNullButGuildIsNotPresent(guildId)
        if (guildResult is DeviousCacheManager.GuildResult.GuildNotPresent) {
            logger.warn { "Received message received for a guild that we don't have in our cache! Guild ID: $guildId; Channel ID: ${event.message.channelId}" }
            return null
        }
        val guild = (guildResult as? DeviousCacheManager.GuildResult.GuildPresent)?.guild

        val author = m.getCacheManager().createUser(event.message.author, !isWebhook)

        // Webhooks do exist as a user (sort of)
        val member = if (isWebhook)
            null
        else
            guild?.let {
                m.getCacheManager().createMember(
                    author,
                    guild,
                    event.message.member.value!!
                )
            } // Should not be null in a guild

        val message = Message(
            m,
            channel,
            author,
            member,
            guild,
            DeviousMessageFragmentData.from(event.message)
        )

        return MessageReceivedEvent(
            m,
            gateway,
            author,
            message,
            channel,
            guild,
            member,
            event
        )
    }

    suspend fun create(gateway: DeviousGateway, event: MessageDelete): MessageDeleteEvent? {
        val guildId = event.message.guildId.value

        val channel = m.getChannelById(event.message.channelId)
        if (channel == null) {
            logger.warn { "Received message delete for a channel that we don't have in our cache! Guild ID: $guildId; Channel ID: ${event.message.channelId};" }
            return null
        }
        val guildResult = m.getCacheManager().getGuildFailIfSnowflakeIsNotNullButGuildIsNotPresent(guildId)
        if (guildResult is DeviousCacheManager.GuildResult.GuildNotPresent) {
            logger.warn { "Received message delete for a guild that we don't have in our cache! Channel ID: ${event.message.channelId}; Guild ID: $guildId;" }
            return null
        }
        val guild = (guildResult as? DeviousCacheManager.GuildResult.GuildPresent)?.guild

        return MessageDeleteEvent(
            m,
            gateway,
            event.message.id,
            channel,
            guild
        )
    }

    suspend fun create(gateway: DeviousGateway, event: MessageDeleteBulk): MessageBulkDeleteEvent? {
        val guildId = event.messageBulk.guildId.value

        val channel = m.getChannelById(event.messageBulk.channelId)
        if (channel == null) {
            logger.warn { "Received bulk delete for a channel that we don't have in our cache! Guild ID: $guildId; Channel ID: ${event.messageBulk.channelId}" }
            return null
        }
        val guildResult = m.getCacheManager().getGuildFailIfSnowflakeIsNotNullButGuildIsNotPresent(guildId)
        if (guildResult is DeviousCacheManager.GuildResult.GuildNotPresent) {
            logger.warn { "Received bulk delete  for a guild that we don't have in our cache! Guild ID: $guildId; Channel ID: ${event.messageBulk.channelId}" }
            return null
        }
        val guild = (guildResult as? DeviousCacheManager.GuildResult.GuildPresent)?.guild

        return MessageBulkDeleteEvent(
            m,
            gateway,
            event.messageBulk.ids,
            channel,
            guild
        )
    }

    suspend fun create(gateway: DeviousGateway, event: MessageReactionAdd): MessageReactionAddEvent? {
        val guildId = event.reaction.guildId.value
        val channelId = event.reaction.channelId
        val userId = event.reaction.userId
        val messageId = event.reaction.messageId

        val channel = m.getChannelById(channelId)
        if (channel == null) {
            logger.warn { "Received reaction add for a channel that we don't have in our cache! Guild ID: $guildId; Channel ID: $channelId; User ID: $userId" }
            return null
        }
        val guildResult = m.getCacheManager().getGuildFailIfSnowflakeIsNotNullButGuildIsNotPresent(guildId)
        if (guildResult is DeviousCacheManager.GuildResult.GuildNotPresent) {
            logger.warn { "Received reaction add for a guild that we don't have in our cache! Guild ID: $guildId; Channel ID: $channelId; User ID: $userId" }
            return null
        }
        val guild = (guildResult as? DeviousCacheManager.GuildResult.GuildPresent)?.guild
        val user = m.getUserById(userId)
        if (user == null) {
            logger.warn { "Received reaction add for a user that we don't have in our cache! Guild ID: $guildId; Channel ID: $channelId; User ID: $userId" }
            return null
        }
        val member = guild?.getMemberById(userId.toLong())

        return MessageReactionAddEvent(
            m,
            gateway,
            user,
            messageId,
            channel,
            MessageReaction(m, channelId, messageId, event.reaction.emoji, null),
            guild,
            member,
        )
    }

    suspend fun create(gateway: DeviousGateway, event: MessageReactionRemove): MessageReactionRemoveEvent? {
        val guildId = event.reaction.guildId.value
        val channelId = event.reaction.channelId
        val userId = event.reaction.userId
        val messageId = event.reaction.messageId

        val channel = m.getChannelById(channelId)
        if (channel == null) {
            logger.warn { "Received reaction remove for a channel that we don't have in our cache! Guild ID: $guildId; Channel ID: $channelId; User ID: $userId" }
            return null
        }
        val guildResult = m.getCacheManager().getGuildFailIfSnowflakeIsNotNullButGuildIsNotPresent(guildId)
        if (guildResult is DeviousCacheManager.GuildResult.GuildNotPresent) {
            logger.warn { "Received reaction remove for a guild that we don't have in our cache! Guild ID: $guildId; Channel ID: $channelId; User ID: $userId" }
            return null
        }
        val guild = (guildResult as? DeviousCacheManager.GuildResult.GuildPresent)?.guild
        val user = m.getUserById(userId)
        if (user == null) {
            logger.warn { "Received reaction remove for a user that we don't have in our cache! Guild ID: $guildId; Channel ID: $channelId; User ID: $userId" }
            return null
        }
        val member = guild?.getMemberById(userId.toLong())

        return MessageReactionRemoveEvent(
            m,
            gateway,
            user,
            messageId,
            channel,
            MessageReaction(m, channelId, messageId, event.reaction.emoji, null),
            guild,
            member,
        )
    }

    suspend fun createGuildJoinEvent(gateway: DeviousGateway, guild: Guild): GuildJoinEvent {
        return GuildJoinEvent(
            m,
            gateway,
            guild
        )
    }

    suspend fun createGuildLeaveEvent(
        gateway: DeviousGateway,
        guild: Guild,
        guildDelete: GuildDelete
    ): GuildLeaveEvent {
        return GuildLeaveEvent(
            m,
            gateway,
            guild,
            guildDelete
        )
    }
}