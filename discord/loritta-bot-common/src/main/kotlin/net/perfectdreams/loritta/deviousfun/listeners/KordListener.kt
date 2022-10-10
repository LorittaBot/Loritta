package net.perfectdreams.loritta.deviousfun.listeners

import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.value
import dev.kord.gateway.*
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.gateway.modules.DiscordCacheModule
import net.perfectdreams.loritta.cinnamon.discord.utils.entitycache.PuddingGuildVoiceState
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.hgetByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.hsetByteArray
import net.perfectdreams.loritta.common.utils.extensions.getPathFromResources
import net.perfectdreams.loritta.deviousfun.JDA
import net.perfectdreams.loritta.deviousfun.cache.DeviousMessageFragmentData
import net.perfectdreams.loritta.deviousfun.entities.Message
import net.perfectdreams.loritta.deviousfun.events.Event
import net.perfectdreams.loritta.deviousfun.events.guild.GuildReadyEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberJoinEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberRemoveEvent
import net.perfectdreams.loritta.deviousfun.events.message.update.MessageUpdateEvent
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway
import net.perfectdreams.loritta.deviousfun.gateway.on
import net.perfectdreams.loritta.deviousfun.hooks.ListenerAdapter
import net.perfectdreams.loritta.deviousfun.utils.DeviousUserUtils
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.cache.decode
import net.perfectdreams.loritta.morenitta.cache.encode
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import redis.clients.jedis.Response
import kotlin.io.path.readText
import kotlin.reflect.KFunction2
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
class KordListener(
    val m: JDA,
    val gateway: DeviousGateway
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val shardId: Int
        get() = gateway.shardId
    private val cacheManager = m.cacheManager
    private var alreadyTriggeredGuildReadyOnStartup = false

    init {
        gateway.on<Ready> {
            // This is used to avoid triggering the onGuildReady spam on subsequent Resumes on full shard login
            alreadyTriggeredGuildReadyOnStartup = true

            logger.info { "Shard $shardId is connected and ready!" }

            val currentRandomKey = gateway.identifyRateLimiter.currentRandomKey

            m.loritta.redisTransaction {
                it.hset(m.loritta.redisKeys.discordGatewaySessions(shardId), "sessionId", this.data.sessionId)
                it.hset(m.loritta.redisKeys.discordGatewaySessions(shardId), "resumeGatewayUrl", this.data.resumeGatewayUrl)
                it.hset(m.loritta.redisKeys.discordGatewaySessions(shardId), "sequence", (this.sequence ?: 0).toString())
            }

            // After it is ready, we will wait 5000ms to release the lock
            delay(5_000)

            var lockResponse: Any? = null
            var lockStatus: Long? = null

            logger.info { "Trying to release lock for bucket ${gateway.identifyRateLimiter.bucketId} (shard $shardId)..." }
            m.loritta.redisConnection {
                if (currentRandomKey != null) {
                    lockResponse = it.eval(
                        LorittaBot::class.getPathFromResources("/redis_delete_lock.lua")!!.readText(),
                        listOf(m.loritta.redisKeys.discordGatewayConcurrentLogin(gateway.identifyRateLimiter.bucketId)),
                        listOf(currentRandomKey)
                    )
                } else {
                    logger.warn { "Couldn't release lock for bucket ${gateway.identifyRateLimiter.bucketId} (shard $shardId) because the current random key is null! Bug?" }
                    lockStatus = -1
                }
            }

            val response = lockResponse ?: lockStatus ?: error("Unknown lock status!")

            when (response) {
                1L -> {
                    logger.info { "Successfully released lock for bucket ${gateway.identifyRateLimiter.bucketId} (shard $shardId)!" }
                }
                0L -> {
                    logger.warn { "Couldn't release lock for bucket ${gateway.identifyRateLimiter.bucketId} (shard $shardId) because our random key does not match!" }
                }
                -1L -> {
                    logger.warn { "Couldn't release lock for bucket ${gateway.identifyRateLimiter.bucketId} (shard $shardId) because our random key is null! Bug?" }
                }
                else -> error("Unknown Response $response")
            }
        }

        gateway.on<DispatchEvent> {
            m.loritta.redisConnection {
                it.hset(m.loritta.redisKeys.discordGatewaySessions(shardId), "sequence", (this.sequence ?: 0).toString())
            }
        }

        gateway.on<Resumed> {
            logger.info { "Shard $shardId resumed!" }

            if (!alreadyTriggeredGuildReadyOnStartup)
                return@on

            alreadyTriggeredGuildReadyOnStartup = true

            val guildIdsOnThisShard = m.loritta.redisConnection {
                // Get all guilds related to this cluster...
                it.hkeys(m.loritta.redisKeys.discordGuilds())
                    .filter {
                        DiscordUtils.getShardIdFromGuildId(
                            it.toLong(),
                            m.loritta.config.loritta.discord.maxShards
                        ) == shardId
                    }
                    .map { Snowflake(it) }
            }

            for (guildId in guildIdsOnThisShard) {
                val guild = cacheManager.getGuild(guildId)
                    ?: continue // Should NOT be null, but if it is, then let's just pretend that it doesn't exist
                val event = GuildReadyEvent(m, gateway, guild)
                forEachListeners(event, ListenerAdapter::onGuildReady)
            }
        }

        gateway.on<GuildCreate> {
            val (guild, duration) = measureTimedValue {
                // Is the guild cached?
                val isGuildCached = m.loritta.redisConnection {
                    it.hexists(m.loritta.redisKeys.discordGuilds(), this.guild.id.toString())
                }

                // Even if it is cached, we will create a guild entity here to update the guild on the cache
                val guild = cacheManager.createGuild(this.guild, this.guild.channels.value)

                if (!isGuildCached) {
                    val event = m.eventFactory.createGuildJoinEvent(gateway, guild, this)

                    forEachListeners(event, ListenerAdapter::onGuildJoin)
                }

                val lorittaVoiceState = this.guild.voiceStates.value?.firstOrNull { it.userId == m.loritta.config.loritta.discord.applicationId }
                if (lorittaVoiceState != null) {
                    // Wait... that's us! omg omg omg
                    val lorittaVoiceConnection = m.loritta.voiceConnectionsManager.voiceConnections[this.guild.id]

                    if (lorittaVoiceConnection == null) {
                        // B-but... we shouldn't be here!? Uhhh, meow? Time to disconnect maybe??
                        logger.warn { "Looks like we are connected @ ${this.guild.id} but we don't have any active voice connections here! We will disconnect from the voice channel to avoid issues..." }
                        val gatewayProxy = m.gatewayManager.getGatewayForGuild(this.guild.id)
                        gatewayProxy.kordGateway.send(
                            UpdateVoiceStatus(
                                this.guild.id,
                                null,
                                selfMute = false,
                                selfDeaf = false
                            )
                        )
                    }
                }

                return@measureTimedValue guild
            }

            logger.info { "GuildCreate for ${guild.idSnowflake} (shard $shardId) took $duration!" }

            // Guild is ready!
            val event = GuildReadyEvent(m, gateway, guild)
            forEachListeners(event, ListenerAdapter::onGuildReady)
        }

        gateway.on<GuildUpdate> {
            // Create a guild entity here to update the guild on the cache
            cacheManager.createGuild(this.guild, this.guild.channels.value)

            // We won't trigger a guild create or a guild ready, because that would be too much hassle (Example: How would we check if we already fired a GuildReady?)
        }

        gateway.on<GuildDelete> {
            logger.info { "Someone removed me @ ${this.guild.id}! :(" }

            val guild = cacheManager.getGuild(this.guild.id) ?: return@on

            // Ignore if it is an availability issue
            if (this.guild.unavailable.value != null)
                return@on

            val event = m.eventFactory.createGuildLeaveEvent(gateway, guild, this)

            forEachListeners(event, ListenerAdapter::onGuildLeave)

            m.cacheManager.deleteGuild(guild.idSnowflake)
        }

        gateway.on<MessageCreate> {
            val event = m.eventFactory.createMessageReceived(gateway, this)

            forEachListeners(event, ListenerAdapter::onMessageReceived)
        }

        gateway.on<MessageUpdate> {
            val isWebhook = DeviousUserUtils.isSenderWebhookOrSpecial(this.message)
            val guildId = this.message.guildId.value

            val channel = m.retrieveChannelById(this.message.channelId)
            val guild = guildId?.let { m.retrieveGuildById(it) }

            // The author may be null, but in this case we will ignore it
            // (The author can be null if it was an "embed update", example: When pasting a URL in chat)
            val authorData = this.message.author.value ?: return@on
            val author = cacheManager.createUser(authorData, !isWebhook)

            // Webhooks do exist as a user (sort of)
            val member = if (isWebhook)
                null
            else
                guild?.let { m.retrieveMemberById(guild, author.idSnowflake) } // The member data is not present when updating a message

            val message = Message(
                m,
                channel,
                author,
                member,
                guild,
                DeviousMessageFragmentData.from(this.message)
            )

            val event = MessageUpdateEvent(
                m,
                gateway,
                author,
                message,
                channel,
                guild,
                member,
                this
            )

            forEachListeners(event, ListenerAdapter::onMessageUpdate)
        }

        gateway.on<MessageDelete> {
            val event = m.eventFactory.create(gateway, this)

            forEachListeners(event, ListenerAdapter::onMessageDelete)
        }

        gateway.on<MessageReactionAdd> {
            val event = m.eventFactory.create(gateway, this)

            forEachListeners(event, ListenerAdapter::onGenericMessageReaction)
        }

        gateway.on<MessageReactionRemove> {
            val event = m.eventFactory.create(gateway, this)

            forEachListeners(event, ListenerAdapter::onGenericMessageReaction)
        }

        gateway.on<MessageDeleteBulk> {
            val event = m.eventFactory.create(gateway, this)

            forEachListeners(event, ListenerAdapter::onMessageBulkDelete)
        }

        gateway.on<GuildMemberAdd> {
            val guild = cacheManager.getGuild(this.member.guildId) ?: return@on
            val userData = this.member.user.value ?: return@on

            // Create user instance, this will store the user in cache
            val user = cacheManager.createUser(userData, true)

            // Create member instance, this will store the member in cache
            val member = cacheManager.createMember(user, guild, this.member)

            val event = GuildMemberJoinEvent(m, gateway, guild, user, member)

            forEachListeners(event, ListenerAdapter::onGuildMemberJoin)
        }

        gateway.on<GuildMemberUpdate> {
            val guild = cacheManager.getGuild(this.member.guildId) ?: return@on
            val userData = this.member.user

            // Create user instance, this will store the user in cache
            val user = cacheManager.createUser(userData, true)

            // Create member instance, this will store the member in cache
            val member = cacheManager.createMember(user, guild, this.member)

            val event = GuildMemberJoinEvent(m, gateway, guild, user, member)

            forEachListeners(event, ListenerAdapter::onGuildMemberJoin)
        }

        gateway.on<GuildMemberRemove> {
            val guild = cacheManager.getGuild(this.member.guildId) ?: return@on

            // Create user instance, this will store the user in cache
            val user = cacheManager.createUser(this.member.user, true)

            // Delete the member, we don't need them anymore
            cacheManager.deleteMember(guild, this.member.user.id)

            val event = GuildMemberRemoveEvent(m, gateway, guild, user)

            forEachListeners(event, ListenerAdapter::onGuildMemberRemove)
        }

        gateway.on<GuildEmojisUpdate> {
            val updatedEmojisData = this.emoji
            val emojis = cacheManager.convertStuff(updatedEmojisData.emojis)

            m.loritta.redisTransaction {
                cacheManager.storeEmojis(
                    it,
                    updatedEmojisData.guildId,
                    emojis
                )
            }
        }

        gateway.on<GuildRoleCreate> {
            val role = this.role.role
            val guild = cacheManager.getGuild(this.role.guildId) ?: return@on

            // Create role instance, this will store the role in cache
            cacheManager.createRole(guild, role)
        }

        gateway.on<GuildRoleUpdate> {
            val role = this.role.role
            val guild = cacheManager.getGuild(this.role.guildId) ?: return@on

            // Create role instance, this will store the role in cache
            cacheManager.createRole(guild, role)
        }

        gateway.on<GuildRoleDelete> {
            val roleId = this.role.id
            val guild = cacheManager.getGuild(this.role.guildId) ?: return@on

            // Delete role instance from cache
            cacheManager.deleteRole(guild, roleId)
        }

        gateway.on<ChannelCreate> {
            val guildId = this.channel.guildId.value ?: return@on
            val guild = cacheManager.getGuild(guildId) ?: return@on

            // Create channel instance, this will store the channel in cache
            cacheManager.createChannel(guild, this.channel)
        }

        gateway.on<ChannelUpdate> {
            val guildId = this.channel.guildId.value ?: return@on
            val guild = cacheManager.getGuild(guildId) ?: return@on

            // Create channel instance, this will store the channel in cache
            cacheManager.createChannel(guild, this.channel)
        }

        gateway.on<ChannelDelete> {
            val guildId = this.channel.guildId.value ?: return@on
            val guild = cacheManager.getGuild(guildId) ?: return@on

            // Delete channel, this will delete the channel from cache
            cacheManager.deleteChannel(guild, this.channel.id)
        }

        gateway.on<VoiceStateUpdate> {
            val guildId = this.voiceState.guildId.value!! // Shouldn't be null here
            val channelId = this.voiceState.channelId
            val userId = this.voiceState.userId

            val currentVoiceState = m.loritta.redisConnection {
                it.hgetByteArray(m.loritta.redisKeys.discordGuildVoiceStates(guildId), voiceState.userId.toString())
            }?.let { m.loritta.binaryCacheTransformers.voiceStates.decode(it) }

            if (userId == m.loritta.config.loritta.discord.applicationId) {
                // Wait... that's us! omg omg omg
                val lorittaVoiceConnection = m.loritta.voiceConnectionsManager.voiceConnections[guildId]
                if (lorittaVoiceConnection != null) {
                    if (channelId != null) {
                        logger.info { "We were moved @ $guildId to $channelId, updating our voice connection to match it..." }
                        lorittaVoiceConnection.channelId = channelId
                    } else {
                        logger.info { "We were removed from a voice channel @ $guildId, shutting down our voice connection..." }
                        m.loritta.voiceConnectionsManager.shutdownVoiceConnection(guildId, lorittaVoiceConnection)
                    }
                }
            }

            val voiceState = this.voiceState

            m.loritta.redisConnection {
                // Channel is null, so let's delete voice states in the guild related to the user
                if (channelId == null) {
                    it.hdel(m.loritta.redisKeys.discordGuildVoiceStates(guildId), voiceState.userId.toString())
                } else {
                    // Channel is not null, let's upsert
                    it.hsetByteArray(
                        m.loritta.redisKeys.discordGuildVoiceStates(guildId),
                        userId.toString(),
                        m.loritta.binaryCacheTransformers.voiceStates.encode(
                            PuddingGuildVoiceState(
                                channelId,
                                userId
                            )
                        )
                    )
                }
            }

            // TODO: Voice events
        }
    }

    private fun <T : Event> forEachListeners(event: T, method: KFunction2<ListenerAdapter, T, Unit>) {
        for (listener in m.listeners) {
            try {
                method.invoke(listener, event)
            } catch (e: Throwable) {
                logger.warn(e) { "Something went wrong while sending ${method.name} to $listener!" }
            }
        }
    }
}