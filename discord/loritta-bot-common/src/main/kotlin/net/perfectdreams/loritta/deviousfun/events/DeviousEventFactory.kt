package net.perfectdreams.loritta.deviousfun.events

import dev.kord.gateway.*
import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.cache.DeviousMessageFragmentData
import net.perfectdreams.loritta.deviousfun.entities.*
import net.perfectdreams.loritta.deviousfun.events.guild.GuildJoinEvent
import net.perfectdreams.loritta.deviousfun.events.guild.GuildLeaveEvent
import net.perfectdreams.loritta.deviousfun.events.message.create.MessageReceivedEvent
import net.perfectdreams.loritta.deviousfun.events.message.delete.MessageBulkDeleteEvent
import net.perfectdreams.loritta.deviousfun.events.message.delete.MessageDeleteEvent
import net.perfectdreams.loritta.deviousfun.events.message.react.MessageReactionAddEvent
import net.perfectdreams.loritta.deviousfun.events.message.react.MessageReactionRemoveEvent
import net.perfectdreams.loritta.deviousfun.events.message.update.MessageUpdateEvent
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway

/**
 * Creates Devious events based off Kord's gateway events
 */
class DeviousEventFactory(val m: JDA) {
    suspend fun createMessageReceived(gateway: DeviousGateway, event: MessageCreate): MessageReceivedEvent {
        val isWebhook = event.message.webhookId.value != null
        val guildId = event.message.guildId.value

        val channel = m.retrieveChannelById(event.message.channelId)
        val guild = guildId?.let { m.retrieveGuildById(it) }

        val author = m.cacheManager.createUser(event.message.author, !isWebhook)

        // Webhooks do exist as a user (sort of)
        val member = if (isWebhook)
            null
        else
            guild?.let { m.cacheManager.createMember(author, guild, event.message.member.value!!) } // Should not be null in a guild

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

    suspend fun create(gateway: DeviousGateway, event: MessageDelete): MessageDeleteEvent {
        val guildId = event.message.guildId.value

        val channel = m.retrieveChannelById(event.message.channelId)
        val guild = guildId?.let { m.retrieveGuildById(it) }

        return MessageDeleteEvent(
            m,
            gateway,
            event.message.id,
            channel,
            guild
        )
    }

    suspend fun create(gateway: DeviousGateway, event: MessageDeleteBulk): MessageBulkDeleteEvent {
        val guildId = event.messageBulk.guildId.value

        val channel = m.retrieveChannelById(event.messageBulk.channelId)
        val guild = guildId?.let { m.retrieveGuildById(it) }

        return MessageBulkDeleteEvent(
            m,
            gateway,
            event.messageBulk.ids,
            channel,
            guild
        )
    }

    suspend fun create(gateway: DeviousGateway, event: MessageReactionAdd): MessageReactionAddEvent {
        val guildId = event.reaction.guildId.value
        val channelId = event.reaction.channelId
        val userId = event.reaction.userId
        val messageId = event.reaction.messageId

        val channel = m.retrieveChannelById(channelId)
        val guild = guildId?.let { m.retrieveGuildById(it) }
        val user = m.retrieveUserById(userId)
        val member = guild?.retrieveMemberById(userId.toLong())

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

    suspend fun create(gateway: DeviousGateway, event: MessageReactionRemove): MessageReactionRemoveEvent {
        val guildId = event.reaction.guildId.value
        val channelId = event.reaction.channelId
        val userId = event.reaction.userId
        val messageId = event.reaction.messageId

        val channel = m.retrieveChannelById(channelId)
        val guild = guildId?.let { m.retrieveGuildById(it) }
        val user = m.retrieveUserById(userId)
        val member = guild?.retrieveMemberById(userId.toLong())

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

    suspend fun createGuildJoinEvent(gateway: DeviousGateway, guild: Guild, guildCreate: GuildCreate): GuildJoinEvent {
        return GuildJoinEvent(
            m,
            gateway,
            guild,
            guildCreate
        )
    }

    suspend fun createGuildLeaveEvent(gateway: DeviousGateway, guild: Guild, guildDelete: GuildDelete): GuildLeaveEvent {
        return GuildLeaveEvent(
            m,
            gateway,
            guild,
            guildDelete
        )
    }
}