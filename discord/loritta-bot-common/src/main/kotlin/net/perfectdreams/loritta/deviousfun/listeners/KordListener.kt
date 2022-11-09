package net.perfectdreams.loritta.deviousfun.listeners

import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.value
import dev.kord.gateway.*
import kotlinx.coroutines.delay
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.tables.ConcurrentLoginBuckets
import net.perfectdreams.loritta.deviouscache.data.*
import net.perfectdreams.loritta.deviouscache.requests.*
import net.perfectdreams.loritta.deviouscache.responses.GetGuildIdsOfShardResponse
import net.perfectdreams.loritta.deviouscache.responses.OkResponse
import net.perfectdreams.loritta.deviouscache.responses.UnlockConflictConcurrentLoginResponse
import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.cache.DatabaseCacheValue
import net.perfectdreams.loritta.deviousfun.cache.DeviousMessageFragmentData
import net.perfectdreams.loritta.deviousfun.entities.Message
import net.perfectdreams.loritta.deviousfun.events.guild.GuildBanEvent
import net.perfectdreams.loritta.deviousfun.events.guild.GuildReadyEvent
import net.perfectdreams.loritta.deviousfun.events.guild.GuildUnbanEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberJoinEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberRemoveEvent
import net.perfectdreams.loritta.deviousfun.events.message.update.MessageUpdateEvent
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway
import net.perfectdreams.loritta.deviousfun.gateway.on
import net.perfectdreams.loritta.deviousfun.hooks.ListenerAdapter
import net.perfectdreams.loritta.deviousfun.utils.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
class KordListener(
    val m: DeviousFun,
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

            logger.info { "Shard $shardId is connected!" }

            // We know what guilds are present in this shard
            val guildsOnThisShard = this.data.guilds.map { it.id }.toSet()
            val cachedGuildsOnThisShard = m.cacheManager.guilds.keys.filter {
                (it shr 22).rem(m.loritta.config.loritta.discord.maxShards).toInt() == shardId
            }.mapNotNull { Snowflake(it) }.toSet()

            // If Loritta was offline when a GuildDelete event was sent, then it means that we have guilds that should be removed from cache, but we don't know about them
            // In this case, we will get all IDs that we have in cache but AREN'T in the Ready event, and then remove them from our cache
            val removedGuilds = cachedGuildsOnThisShard - guildsOnThisShard
            if (removedGuilds.isNotEmpty()) {
                logger.info { "Removing ${removedGuilds.size} $removedGuilds guilds because they aren't present in $shardId's ready event, but we have them cached" }
                for (guildId in removedGuilds)
                    m.cacheManager.deleteGuild(guildId)
            }

            val currentRandomKey = gateway.identifyRateLimiter.currentRandomKey

            m.cacheManager.gatewaySessions[shardId] = DeviousGatewaySession(
                this.data.sessionId,
                this.data.resumeGatewayUrl,
                this.sequence ?: 0
            )

            // After it is ready, we will wait 5000ms to release the lock
            delay(5_000)

            logger.info { "Trying to release lock for bucket ${gateway.identifyRateLimiter.bucketId} (shard $shardId)..." }
            if (currentRandomKey != null) {
                val deletedBucketsCount = m.loritta.newSuspendedTransaction {
                    ConcurrentLoginBuckets.deleteWhere { ConcurrentLoginBuckets.id eq gateway.identifyRateLimiter.bucketId and (ConcurrentLoginBuckets.randomKey eq currentRandomKey) }
                }

                when (deletedBucketsCount) {
                    0 -> logger.warn { "Couldn't release lock for bucket ${gateway.identifyRateLimiter.bucketId} (shard $shardId) because our random key does not match or the bucket was already released!" }
                    else -> logger.info { "Successfully released lock for bucket ${gateway.identifyRateLimiter.bucketId} (shard $shardId)!" }
                }
            } else {
                logger.warn { "Couldn't release lock for bucket ${gateway.identifyRateLimiter.bucketId} (shard $shardId) because the current random key is null! Bug?" }
            }
        }

        gateway.on<DispatchEvent> {
            val sequenceId = this.sequence
            if (sequenceId != null)
                m.cacheManager.gatewaySessions[shardId]?.sequence = sequenceId
        }

        gateway.on<Resumed> {
            logger.info { "Shard $shardId resumed! alreadyTriggeredGuildReadyOnStartup? $alreadyTriggeredGuildReadyOnStartup" }

            // If we already triggered the guild ready on this instance, then we wouldn't trigger it again
            if (alreadyTriggeredGuildReadyOnStartup)
                return@on

            alreadyTriggeredGuildReadyOnStartup = true

            val guildsOnThisShard = m.cacheManager.guilds.keys.filter {
                (it shr 22).rem(m.loritta.config.loritta.discord.maxShards).toInt() == shardId
            }.mapNotNull { cacheManager.getGuild(Snowflake(it)) } // Should NOT be null, but if it is, then let's just pretend that it doesn't exist

            for (guild in guildsOnThisShard) {
                val event = GuildReadyEvent(m, gateway, guild)
                m.forEachListeners(event, ListenerAdapter::onGuildReady)
            }
        }

        gateway.on<GuildCreate> {
            val (guildAndJoinStatus, duration) = measureTimedValue {
                // Even if it is cached, we will create a guildAndJoinStatus entity here to update the guildAndJoinStatus on the cache
                cacheManager.createGuild(this.guild, this.guild.channels.value)
            }

            // logger.info { "GuildCreate for ${guildAndJoinStatus.guild.idSnowflake} (shard $shardId) took $duration!" }

            processGuild(guildAndJoinStatus)
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

            m.forEachListeners(event, ListenerAdapter::onGuildLeave)

            m.cacheManager.deleteGuild(guild.idSnowflake)
        }

        gateway.on<MessageCreate> {
            val event = m.eventFactory.createMessageReceived(gateway, this)

            m.forEachListeners(event, ListenerAdapter::onMessageReceived)
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
                guild?.let {
                    m.retrieveMemberById(
                        guild,
                        author.idSnowflake
                    )
                } // The member data is not present when updating a message

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

            m.forEachListeners(event, ListenerAdapter::onMessageUpdate)
        }

        gateway.on<MessageDelete> {
            val event = m.eventFactory.create(gateway, this)

            m.forEachListeners(event, ListenerAdapter::onMessageDelete)
        }

        gateway.on<MessageReactionAdd> {
            val event = m.eventFactory.create(gateway, this)

            m.forEachListeners(event, ListenerAdapter::onGenericMessageReaction)
        }

        gateway.on<MessageReactionRemove> {
            val event = m.eventFactory.create(gateway, this)

            m.forEachListeners(event, ListenerAdapter::onGenericMessageReaction)
        }

        gateway.on<MessageDeleteBulk> {
            val event = m.eventFactory.create(gateway, this)

            m.forEachListeners(event, ListenerAdapter::onMessageBulkDelete)
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
                guild?.let {
                    m.retrieveMemberById(
                        guild,
                        author.idSnowflake
                    )
                } // The member data is not present when updating a message

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

            m.forEachListeners(event, ListenerAdapter::onMessageUpdate)
        }

        gateway.on<GuildMemberAdd> {
            val guild = cacheManager.getGuild(this.member.guildId) ?: return@on
            val userData = this.member.user.value ?: return@on

            val lightweightGuildId = guild.idSnowflake.toLightweightSnowflake()
            m.cacheManager.withLock(GuildKey(lightweightGuildId)) {
                // Update member count
                val newData = DeviousGuildDataWrapper(
                    guild.guild.copy(
                        memberCount = guild.guild.memberCount + 1
                    )
                )
                m.cacheManager.guilds[lightweightGuildId] = newData
                m.cacheManager.cacheDatabase.queue {
                    this.guilds[lightweightGuildId] = DatabaseCacheValue.Value(newData)
                }
            }

            // Create user instance, this will store the user in cache
            val user = cacheManager.createUser(userData, true)

            // Create member instance, this will store the member in cache
            val member = cacheManager.createMember(user, guild, this.member)

            val event = GuildMemberJoinEvent(m, gateway, guild, user, member)

            m.forEachListeners(event, ListenerAdapter::onGuildMemberJoin)
        }

        gateway.on<GuildMemberUpdate> {
            val guild = cacheManager.getGuild(this.member.guildId) ?: return@on
            val userData = this.member.user

            // Create user instance, this will store the user in cache
            val user = cacheManager.createUser(userData, true)

            // Create member instance, this will store the member in cache
            val member = cacheManager.createMember(user, guild, this.member)
        }

        gateway.on<GuildMemberRemove> {
            val guild = cacheManager.getGuild(this.member.guildId) ?: return@on

            val lightweightGuildId = guild.idSnowflake.toLightweightSnowflake()
            m.cacheManager.withLock(GuildKey(lightweightGuildId)) {
                // Update member count
                val newData = DeviousGuildDataWrapper(
                    guild.guild.copy(
                        memberCount = guild.guild.memberCount - 1
                    )
                )
                m.cacheManager.guilds[lightweightGuildId] = newData
                m.cacheManager.cacheDatabase.queue {
                    this.guilds[lightweightGuildId] = DatabaseCacheValue.Value(newData)
                }
            }

            // Create user instance, this will store the user in cache
            val user = cacheManager.createUser(this.member.user, true)

            // Delete the member, we don't need them anymore
            cacheManager.deleteMember(guild, this.member.user.id)

            val event = GuildMemberRemoveEvent(m, gateway, guild, user)

            m.forEachListeners(event, ListenerAdapter::onGuildMemberRemove)
        }

        gateway.on<GuildBanAdd> {
            val guild = cacheManager.getGuild(this.ban.guildId) ?: return@on

            // Create user instance, this will store the user in cache
            val user = cacheManager.createUser(this.ban.user, true)

            val event = GuildBanEvent(m, gateway, guild, user)

            m.forEachListeners(event, ListenerAdapter::onGuildBan)
        }

        gateway.on<GuildBanRemove> {
            val guild = cacheManager.getGuild(this.ban.guildId) ?: return@on

            // Create user instance, this will store the user in cache
            val user = cacheManager.createUser(this.ban.user, true)

            val event = GuildUnbanEvent(m, gateway, guild, user)

            m.forEachListeners(event, ListenerAdapter::onGuildUnban)
        }

        gateway.on<GuildEmojisUpdate> {
            m.cacheManager.storeEmojis(this.emoji.guildId, this.emoji.emojis.map { DeviousGuildEmojiData.from(it) })
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

            /* val currentVoiceState = m.loritta.redisConnection("getting current voice state of user ${voiceState.userId}") {
                it.hgetByteArray(m.loritta.redisKeys.discordGuildVoiceStates(guildId), voiceState.userId.toString())
            }?.let { m.loritta.binaryCacheTransformers.voiceStates.decode(it) } */

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

            val lightweightGuildId = guildId.toLightweightSnowflake()
            val lightweightChannelId = channelId?.toLightweightSnowflake()
            val lightweightUserId = userId.toLightweightSnowflake()

            cacheManager.withLock(GuildKey(lightweightGuildId), UserKey(lightweightUserId)) {
                cacheManager.awaitForEntityPersistenceModificationMutex()

                logger.info { "Updating voice state of user $lightweightUserId on channel $lightweightChannelId on guild $lightweightGuildId" }

                val currentVoiceStates = cacheManager.voiceStates[lightweightGuildId]
                // Expected 1 because we will insert the new voice state
                val newVoiceStates = (currentVoiceStates ?: SnowflakeMap(1))
                    .also {
                        if (lightweightChannelId != null) {
                            it[lightweightUserId] = DeviousVoiceStateData(
                                lightweightUserId,
                                lightweightChannelId
                            )
                        } else {
                            it.remove(lightweightUserId)
                        }
                    }

                cacheManager.voiceStates[lightweightGuildId] = newVoiceStates

                cacheManager.cacheDatabase.queue {
                    this.voiceStates[lightweightGuildId] = DatabaseCacheValue.Value(newVoiceStates.toMap())
                }
            }

            // TODO: Voice events
        }
    }

    private suspend fun processGuild(guildAndJoinStatus: GuildAndJoinStatus) {
        val (guild, isNewGuild) = guildAndJoinStatus

        if (isNewGuild) {
            val event = m.eventFactory.createGuildJoinEvent(gateway, guild)

            m.forEachListeners(event, ListenerAdapter::onGuildJoin)
        }

        /* val lorittaVoiceState = this.guild.voiceStates.value?.firstOrNull { it.userId == m.loritta.config.loritta.discord.applicationId }
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
        } */

        // logger.info { "GuildCreate for ${guild.idSnowflake} (shard $shardId) took $duration!" }

        // Guild is ready!
        val event = GuildReadyEvent(m, gateway, guild)
        m.forEachListeners(event, ListenerAdapter::onGuildReady)
    }
}