package net.perfectdreams.loritta.deviousfun.listeners

import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.value
import dev.kord.gateway.*
import it.unimi.dsi.fastutil.longs.Long2LongMap
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import mu.KotlinLogging
import net.perfectdreams.exposedpowerutils.sql.transaction
import net.perfectdreams.loritta.deviouscache.data.*
import net.perfectdreams.loritta.deviousfun.DeviousShard
import net.perfectdreams.loritta.deviousfun.cache.*
import net.perfectdreams.loritta.deviousfun.entities.Message
import net.perfectdreams.loritta.deviousfun.events.guild.GuildBanEvent
import net.perfectdreams.loritta.deviousfun.events.guild.GuildReadyEvent
import net.perfectdreams.loritta.deviousfun.events.guild.GuildUnbanEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberJoinEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberRemoveEvent
import net.perfectdreams.loritta.deviousfun.events.message.update.MessageUpdateEvent
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway
import net.perfectdreams.loritta.deviousfun.hooks.ListenerAdapter
import net.perfectdreams.loritta.deviousfun.utils.*
import org.jetbrains.exposed.sql.SchemaUtils
import java.io.File
import java.util.*
import kotlin.reflect.KFunction2
import kotlin.time.ExperimentalTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
class KordListener(val m: DeviousShard) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val replayingEventsLock = Mutex()
    private val gateway: DeviousGateway
        get() = m.deviousGateway
    private val shardId: Int
        get() = gateway.shardId
    private var alreadyTriggeredGuildReadyOnStartup = false

    private suspend fun getCacheManager() = m.getCacheManager()

    fun registerCollect() {
        gateway.kordGateway.launch {
            for (cacheTriggerEvent in m.triggeredEventsDueToCacheUpdate) {
                cacheTriggerEvent.invoke(m)
            }
        }

        gateway.kordGateway.launch {
            gateway.events.collect {
                // This is used to avoid processing events while we are replaying events
                replayingEventsLock.withLock {
                    processEvent(it)
                }
            }
        }
    }

    suspend fun processEvent(it: Event) {
        // Run the events here
        // We use collect instead of gateway.on because we want all events to be executed sequentially, to avoid race conditions when processing events for our cache.
        // If you need to execute something that blocks (or suspends for a long time), dispatch a new coroutine!
        //
        // About the getCacheManager() calls: Because we process events sequentially, we *technically* don't need to worry about the cache manager being replaced off our feet, since the only thing
        // that can trigger a cache manager change is a Ready event
        // TODO: Maybe synchronize cache changes to this call? This way, even if other threads modify the cache, it would still be executed sequentially (sort of)
        try {
            if (it is DispatchEvent) {
                // Update sequence ID
                val sequenceId = it.sequence
                // We use cacheManagerDoNotUseThisUnlessIfYouKnowWhatYouAreDoing here because we don't care if it is null, we don't want to suspend if it is null
                if (sequenceId != null)
                    m.cacheManagerOrNull?.gatewaySession?.sequence = sequenceId
            }

            when (it) {
                is Close -> {
                    gateway.status.value = DeviousGateway.Status.DISCONNECTED

                    logger.info { "Received close event for $shardId - $this" }
                }
                is Ready -> {
                    // This is used to avoid triggering the onGuildReady spam on subsequent Resumes on full shard login
                    alreadyTriggeredGuildReadyOnStartup = true

                    gateway.status.value = DeviousGateway.Status.CONNECTED

                    val guildCount = it.data.guilds.size

                    logger.info { "Shard $shardId is connected! - Guilds: $guildCount" }

                    m.queuedGuildEvents.clear()
                    m.guildsOnThisShard.clear()
                    m.unavailableGuilds.clear()

                    val guildIds = it.data.guilds.map { it.id }
                    m.guildsOnThisShard.addAll(guildIds)
                    m.unavailableGuilds.addAll(guildIds)

                    // Because this is a full Ready reconnect, our cache is considered "stale"
                    // To avoid issues, we will scrap our entire cache and begin from scratch
                    // TODO: Maybe replace this with a MutableStateFlow, then we can change the current cache manager to null and make a "await until it isn't null" method
                    // This way, we don't need to shutdown the stuff in this wonky way (where we need to shutdown the database first, then create a new manager, then shutdown the old manager)
                    // Keep in mind that cache related methods should ONLY BE EXECUTED SEQUENTIALLY (can't be executed in a different thread, maybe do a coroutine check for it?)
                    // So in theory the cache manager will never be replaced "under your feet"
                    logger.info { "Creating new cache for shard $shardId" }
                    val oldCacheManager = m.cacheManagerDoNotUseThisUnlessIfYouKnowWhatYouAreDoing.value
                    // This will suspend all "getCacheManager" calls because this is now null
                    m.cacheManagerDoNotUseThisUnlessIfYouKnowWhatYouAreDoing.value = null
                    oldCacheManager?.stop() // Stop the old cache manager

                    // Delete the old database
                    File("cache/lori_devious_shard_${shardId}.db").delete()

                    // Create the fresh new database
                    // heh, fresh start https://youtu.be/ofQiYxuJCBc
                    val newDatabase = DeviousCacheDatabase.createCacheDatabase(shardId)

                    // Create the tables
                    transaction(Dispatchers.IO, newDatabase) {
                        SchemaUtils.createMissingTablesAndColumns(
                            *DeviousCacheDatabase.cacheTables.toTypedArray()
                        )
                    }

                    // Replace with new instance
                    val newCacheManager = DeviousCacheManager(
                        m,
                        newDatabase,
                        m.triggeredEventsDueToCacheUpdate,
                        SnowflakeMap(0),
                        SnowflakeMap(guildCount),
                        SnowflakeMap(guildCount),
                        Long2LongOpenHashMap(0),
                        SnowflakeMap(guildCount),
                        SnowflakeMap(guildCount),
                        SnowflakeMap(guildCount),
                        SnowflakeMap(guildCount),
                        null
                    )

                    newCacheManager.gatewaySession = DeviousGatewaySession(
                        it.data.sessionId,
                        it.data.resumeGatewayUrl,
                        it.sequence ?: 0
                    )

                    m.cacheManagerDoNotUseThisUnlessIfYouKnowWhatYouAreDoing.value = newCacheManager

                    gateway.kordGateway.editPresence {
                        status = PresenceStatus.Online
                        playing(m.createActivityTextWithShardAndClusterId(m.loritta.config.loritta.discord.activity.name))
                    }
                }
                is Resumed -> {
                    logger.info { "Shard $shardId resumed! alreadyTriggeredGuildReadyOnStartup? $alreadyTriggeredGuildReadyOnStartup" }

                    gateway.status.value = DeviousGateway.Status.CONNECTED

                    gateway.kordGateway.editPresence {
                        status = PresenceStatus.Online
                        playing(m.createActivityTextWithShardAndClusterId(m.loritta.config.loritta.discord.activity.name))
                    }

                    // Add all guilds from the cache on the "guildsOnThisShard" map
                    // The unavailable list won't be filled, but I don't think that's a huge deal?
                    m.guildsOnThisShard.addAll(getCacheManager().guilds.keys.map { Snowflake(it) })

                    // If we already triggered the guild ready on this instance, then we wouldn't trigger it again
                    if (alreadyTriggeredGuildReadyOnStartup)
                        return

                    alreadyTriggeredGuildReadyOnStartup = true

                    val guildsOnThisShard = m.getCacheManager().guilds.keys.filter {
                        (it shr 22).rem(m.loritta.config.loritta.discord.maxShards).toInt() == shardId
                    }.mapNotNull { getCacheManager().getGuild(Snowflake(it)) } // Should NOT be null, but if it is, then let's just pretend that it doesn't exist

                    for (guild in guildsOnThisShard) {
                        val event = GuildReadyEvent(m, gateway, guild)
                        m.forEachListeners(event, ListenerAdapter::onGuildReady)
                    }
                }
                is GuildCreate -> {
                    m.unavailableGuilds.remove(it.guild.id)

                    // The guild join status should be validated by checking the "guildsOnThisShard" list
                    val isNewGuild = m.guildsOnThisShard.contains(it.guild.id)

                    m.guildsOnThisShard.add(it.guild.id)

                    val (guild, duration) = measureTimedValue {
                        // Even if it is cached, we will create a guildAndJoinStatus entity here to update the guildAndJoinStatus on the cache
                        getCacheManager().createGuild(it.guild, it.guild.channels.value)
                    }

                    // logger.info { "GuildCreate for ${guildAndJoinStatus.guild.idSnowflake} (shard $shardId) took $duration!" }

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

                    // After relaying, we will run a replay of all events that we received related to this guild
                    val queuedEvents =  m.queuedGuildEvents[it.guild.id]
                    if (queuedEvents != null) {
                        logger.info { "Replaying ${queuedEvents.size} events for guild ${it.guild.id} on shard $shardId because those events were received before the GuildCreate event" }
                        m.queuedGuildEvents.remove(it.guild.id)
                        while (queuedEvents.isNotEmpty()) {
                            val event = queuedEvents.pop()
                            processEvent(event)
                        }
                    }
                }
                is GuildUpdate -> {
                    if (queueEventIfGuildIsUnavailable(it, it.guild.id))
                        return

                    // Create a guild entity here to update the guild on the cache
                    getCacheManager().createGuild(it.guild, it.guild.channels.value)

                    // We won't trigger a guild create or a guild ready, because that would be too much hassle (Example: How would we check if we already fired a GuildReady?)
                }
                is GuildDelete -> {
                    // Ignore if it is an availability issue
                    // If the unavailable field is not set, the user was removed from the guild.
                    val unavailable = it.guild.unavailable.value

                    m.guildsOnThisShard.remove(it.guild.id)
                    m.queuedGuildEvents.remove(it.guild.id)

                    val guild = getCacheManager().getGuild(it.guild.id) ?: return

                    if (unavailable != null) {
                        logger.info { "Guild ${it.guild.id} is unavailable! ${it.guild.unavailable.value}" }
                    } else {
                        logger.info { "Someone removed me @ ${it.guild.id}! :(" }

                        val event = m.eventFactory.createGuildLeaveEvent(gateway, guild, it)

                        m.forEachListeners(event, ListenerAdapter::onGuildLeave)
                    }

                    m.getCacheManager().deleteGuild(guild.idSnowflake)
                }
                is MessageCreate -> {
                    if (queueEventIfGuildIsUnavailable(it, it.message.guildId.value))
                        return

                    val event = m.eventFactory.createMessageReceived(gateway, it) ?: return

                    m.forEachListeners(event, ListenerAdapter::onMessageReceived)
                }
                is MessageUpdate -> {
                    if (queueEventIfGuildIsUnavailable(it, it.message.guildId.value))
                        return

                    val isWebhook = DeviousUserUtils.isSenderWebhookOrSpecial(it.message)
                    val guildId = it.message.guildId.value

                    val channel = m.getChannelById(it.message.channelId)
                    if (channel == null) {
                        logger.warn { "Received message update for a channel that we don't have in our cache! Channel ID: ${it.message.channelId}; Guild ID: $guildId" }
                        return
                    }
                    val guildResult = getCacheManager().getGuildFailIfSnowflakeIsNotNullButGuildIsNotPresent(guildId)
                    if (guildResult is DeviousCacheManager.GuildResult.GuildNotPresent) {
                        logger.warn { "Received message update for a guild that we don't have in our cache! Channel ID: ${it.message.channelId}; Guild ID: $guildId" }
                        return
                    }
                    val guild = (guildResult as? DeviousCacheManager.GuildResult.GuildPresent)?.guild

                    // The author may be null, but in this case we will ignore it
                    // (The author can be null if it was an "embed update", example: When pasting a URL in chat)
                    val authorData = it.message.author.value ?: return
                    val author = getCacheManager().createUser(authorData, !isWebhook)

                    // Webhooks do exist as a user (sort of)
                    val member = if (isWebhook)
                        null
                    else
                        guild?.let {
                            m.getMemberById(
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
                        DeviousMessageFragmentData.from(it.message)
                    )

                    val event = MessageUpdateEvent(
                        m,
                        gateway,
                        author,
                        message,
                        channel,
                        guild,
                        member,
                        it
                    )

                    m.forEachListeners(event, ListenerAdapter::onMessageUpdate)
                }
                is MessageDelete -> {
                    if (queueEventIfGuildIsUnavailable(it, it.message.guildId.value))
                        return

                    val event = m.eventFactory.create(gateway, it) ?: return

                    m.forEachListeners(event, ListenerAdapter::onMessageDelete)
                }
                is MessageDeleteBulk -> {
                    if (queueEventIfGuildIsUnavailable(it, it.messageBulk.guildId.value))
                        return

                    val event = m.eventFactory.create(gateway, it) ?: return

                    m.forEachListeners(event, ListenerAdapter::onMessageBulkDelete)
                }
                is MessageReactionAdd -> {
                    if (queueEventIfGuildIsUnavailable(it, it.reaction.guildId.value))
                        return

                    val event = m.eventFactory.create(gateway, it) ?: return

                    m.forEachListeners(event, ListenerAdapter::onGenericMessageReaction)
                }
                is MessageReactionRemove -> {
                    if (queueEventIfGuildIsUnavailable(it, it.reaction.guildId.value))
                        return

                    val event = m.eventFactory.create(gateway, it) ?: return

                    m.forEachListeners(event, ListenerAdapter::onGenericMessageReaction)
                }
                is GuildMemberAdd -> {
                    if (queueEventIfGuildIsUnavailable(it, it.member.guildId))
                        return

                    val guild = getCacheManager().getGuild(it.member.guildId)
                    if (guild == null) {
                        logger.warn { "Received guild member add for a guild that we don't have in our cache! Guild ID: ${it.member.guildId}" }
                        return
                    }
                    val userData = it.member.user.value ?: return

                    val lightweightGuildId = guild.idSnowflake.toLightweightSnowflake()
                    m.getCacheManager().withLock(GuildKey(lightweightGuildId)) {
                        // Update member count
                        val newData = DeviousGuildDataWrapper(
                            guild.guild.copy(
                                memberCount = guild.guild.memberCount + 1
                            )
                        )
                        m.getCacheManager().guilds[lightweightGuildId] = newData
                        m.getCacheManager().cacheDatabase.queue {
                            this.guilds[lightweightGuildId] = DatabaseCacheValue.Value(newData)
                        }
                    }

                    // Create user instance, this will store the user in cache
                    val user = getCacheManager().createUser(userData, true)

                    // Create member instance, this will store the member in cache
                    val member = getCacheManager().createMember(user, guild, it.member)

                    val event = GuildMemberJoinEvent(m, gateway, guild, user, member)

                    m.forEachListeners(event, ListenerAdapter::onGuildMemberJoin)
                }
                is GuildMemberUpdate -> {
                    if (queueEventIfGuildIsUnavailable(it, it.member.guildId))
                        return

                    val guild = getCacheManager().getGuild(it.member.guildId)
                    if (guild == null) {
                        logger.warn { "Received guild member update for a guild that we don't have in our cache! Guild ID: ${it.member.guildId}" }
                        return
                    }
                    val userData = it.member.user

                    // Create user instance, this will store the user in cache
                    val user = getCacheManager().createUser(userData, true)

                    // Create member instance, this will store the member in cache
                    val member = getCacheManager().createMember(user, guild, it.member)
                }
                is GuildMemberRemove -> {
                    if (queueEventIfGuildIsUnavailable(it, it.member.guildId))
                        return

                    val guild = getCacheManager().getGuild(it.member.guildId)
                    if (guild == null) {
                        logger.warn { "Received guild member remove for a guild that we don't have in our cache! Guild ID: ${it.member.guildId}" }
                        return
                    }

                    val lightweightGuildId = guild.idSnowflake.toLightweightSnowflake()
                    m.getCacheManager().withLock(GuildKey(lightweightGuildId)) {
                        // Update member count
                        val newData = DeviousGuildDataWrapper(
                            guild.guild.copy(
                                memberCount = guild.guild.memberCount - 1
                            )
                        )
                        m.getCacheManager().guilds[lightweightGuildId] = newData
                        m.getCacheManager().cacheDatabase.queue {
                            this.guilds[lightweightGuildId] = DatabaseCacheValue.Value(newData)
                        }
                    }

                    // Create user instance, this will store the user in cache
                    val user = getCacheManager().createUser(it.member.user, true)

                    // Delete the member, we don't need them anymore
                    getCacheManager().deleteMember(guild, it.member.user.id)

                    val event = GuildMemberRemoveEvent(m, gateway, guild, user)

                    m.forEachListeners(event, ListenerAdapter::onGuildMemberRemove)
                }
                is GuildBanAdd -> {
                    if (queueEventIfGuildIsUnavailable(it, it.ban.guildId))
                        return

                    val guild = getCacheManager().getGuild(it.ban.guildId)
                    if (guild == null) {
                        logger.warn { "Received guild ban add for a guild that we don't have in our cache! Guild ID: ${it.ban.guildId}" }
                        return
                    }

                    // Create user instance, this will store the user in cache
                    val user = getCacheManager().createUser(it.ban.user, true)

                    val event = GuildBanEvent(m, gateway, guild, user)

                    m.forEachListeners(event, ListenerAdapter::onGuildBan)
                }
                is GuildBanRemove -> {
                    if (queueEventIfGuildIsUnavailable(it, it.ban.guildId))
                        return

                    val guild = getCacheManager().getGuild(it.ban.guildId)
                    if (guild == null) {
                        logger.warn { "Received guild ban remove for a guild that we don't have in our cache! Guild ID: ${it.ban.guildId}" }
                        return
                    }

                    // Create user instance, this will store the user in cache
                    val user = getCacheManager().createUser(it.ban.user, true)

                    val event = GuildUnbanEvent(m, gateway, guild, user)

                    m.forEachListeners(event, ListenerAdapter::onGuildUnban)
                }
                is GuildEmojisUpdate -> {
                    if (queueEventIfGuildIsUnavailable(it, it.emoji.guildId))
                        return

                    m.getCacheManager().storeEmojis(it.emoji.guildId, it.emoji.emojis.map { DeviousGuildEmojiData.from(it) })
                }
                is GuildRoleCreate -> {
                    if (queueEventIfGuildIsUnavailable(it, it.role.guildId))
                        return

                    val role = it.role.role
                    val guild = getCacheManager().getGuild(it.role.guildId)
                    if (guild == null) {
                        logger.warn { "Received guild role create for a guild that we don't have in our cache! Guild ID: ${it.role.guildId}" }
                        return
                    }

                    // Create role instance, this will store the role in cache
                    getCacheManager().createRole(guild, role)
                }
                is GuildRoleUpdate -> {
                    if (queueEventIfGuildIsUnavailable(it, it.role.guildId))
                        return

                    val role = it.role.role
                    val guild = getCacheManager().getGuild(it.role.guildId)
                    if (guild == null) {
                        logger.warn { "Received guild role update for a guild that we don't have in our cache! Guild ID: ${it.role.guildId}" }
                        return
                    }

                    // Create role instance, this will store the role in cache
                    getCacheManager().createRole(guild, role)
                }
                is GuildRoleDelete -> {
                    if (queueEventIfGuildIsUnavailable(it, it.role.guildId))
                        return

                    val roleId = it.role.id
                    val guild = getCacheManager().getGuild(it.role.guildId)
                    if (guild == null) {
                        logger.warn { "Received guild role delete for a guild that we don't have in our cache! Guild ID: ${it.role.guildId}" }
                        return
                    }

                    // Delete role instance from cache
                    getCacheManager().deleteRole(guild, roleId)
                }
                is ChannelCreate -> {
                    if (queueEventIfGuildIsUnavailable(it, it.channel.guildId.value))
                        return

                    val guildId = it.channel.guildId.value ?: return
                    val guild = getCacheManager().getGuild(guildId)
                    if (guild == null) {
                        logger.warn { "Received channel create for a guild that we don't have in our cache! Guild ID: ${it.channel.guildId}; Channel ID: ${it.channel.id}" }
                        return
                    }

                    // Create channel instance, this will store the channel in cache
                    getCacheManager().createChannel(guild, it.channel)
                }
                is ChannelUpdate -> {
                    if (queueEventIfGuildIsUnavailable(it, it.channel.guildId.value))
                        return

                    val guildId = it.channel.guildId.value ?: return
                    val guild = getCacheManager().getGuild(guildId)
                    if (guild == null) {
                        logger.warn { "Received channel update for a guild that we don't have in our cache! Guild ID: ${it.channel.guildId}; Channel ID: ${it.channel.id}" }
                        return
                    }

                    // Create channel instance, this will store the channel in cache
                    getCacheManager().createChannel(guild, it.channel)
                }
                is ChannelDelete -> {
                    if (queueEventIfGuildIsUnavailable(it, it.channel.guildId.value))
                        return

                    val guildId = it.channel.guildId.value ?: return
                    val guild = getCacheManager().getGuild(guildId)
                    if (guild == null) {
                        logger.warn { "Received channel delete for a guild that we don't have in our cache! Guild ID: ${it.channel.guildId}; Channel ID: ${it.channel.id}" }
                        return
                    }

                    // Delete channel, this will delete the channel from cache
                    getCacheManager().deleteChannel(guild, it.channel.id)
                }
                is VoiceStateUpdate -> {
                    if (queueEventIfGuildIsUnavailable(it, it.voiceState.guildId.value))
                        return

                    val guildId = it.voiceState.guildId.value!! // Shouldn't be null here
                    val channelId = it.voiceState.channelId
                    val userId = it.voiceState.userId

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

                    getCacheManager().withLock(GuildKey(lightweightGuildId), UserKey(lightweightUserId)) {
                        getCacheManager().awaitForEntityPersistenceModificationMutex()

                        logger.info { "Updating voice state of user $lightweightUserId on channel $lightweightChannelId on guild $lightweightGuildId" }

                        val currentVoiceStates = getCacheManager().voiceStates[lightweightGuildId]
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

                        getCacheManager().voiceStates[lightweightGuildId] = newVoiceStates

                        getCacheManager().cacheDatabase.queue {
                            this.voiceStates[lightweightGuildId] = DatabaseCacheValue.Value(newVoiceStates.toMap())
                        }
                    }

                    // TODO: Voice events
                }
                else -> {}
            }
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while trying to process ${this::class.simpleName}" }
        }
    }

    private fun queueEventIfGuildIsUnavailable(event: Event, guildId: Snowflake?): Boolean {
        // Not a guild
        if (guildId == null)
            return false

        if (!m.guildsOnThisShard.contains(guildId)) {
            // This should never happen, but...
            logger.warn { "Event ${event::class.simpleName} depends on guild $guildId, but guild $guildId is not present on this shard! Queueing..." }
            m.queuedGuildEvents.getOrPut(guildId) { LinkedList<Event>() }
                .also { it.add(event) }
            return true
        }
        if (m.unavailableGuilds.contains(guildId)) {
            logger.warn { "Event ${event::class.simpleName} depends on guild $guildId, but guild $guildId is unavailable! Queueing..." }
            m.queuedGuildEvents.getOrPut(guildId) { LinkedList<Event>() }
                .also { it.add(event) }
            return true
        }
        return false
    }
}