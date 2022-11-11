package net.perfectdreams.loritta.deviousfun.listeners

import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.value
import dev.kord.gateway.*
import kotlinx.coroutines.launch
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.data.*
import net.perfectdreams.loritta.deviousfun.DeviousFun
import net.perfectdreams.loritta.deviousfun.cache.DatabaseCacheValue
import net.perfectdreams.loritta.deviousfun.cache.DeviousCacheManager
import net.perfectdreams.loritta.deviousfun.cache.DeviousMessageFragmentData
import net.perfectdreams.loritta.deviousfun.entities.Message
import net.perfectdreams.loritta.deviousfun.events.DeviousEventFactory
import net.perfectdreams.loritta.deviousfun.events.guild.GuildBanEvent
import net.perfectdreams.loritta.deviousfun.events.guild.GuildReadyEvent
import net.perfectdreams.loritta.deviousfun.events.guild.GuildUnbanEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberJoinEvent
import net.perfectdreams.loritta.deviousfun.events.guild.member.GuildMemberRemoveEvent
import net.perfectdreams.loritta.deviousfun.events.message.update.MessageUpdateEvent
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway
import net.perfectdreams.loritta.deviousfun.hooks.ListenerAdapter
import net.perfectdreams.loritta.deviousfun.utils.*
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
        gateway.kordGateway.launch {
            gateway.events.collect {
                // Run the events here
                // We use collect instead of gateway.on because we want all events to be executed sequentially, to avoid race conditions when processing events for our cache.
                // If you need to execute something that blocks (or suspends for a long time), dispatch a new coroutine!
                try {
                    if (it is DispatchEvent) {
                        // Update sequence ID
                        val sequenceId = it.sequence
                        if (sequenceId != null)
                            m.cacheManager.gatewaySessions[shardId]?.sequence = sequenceId
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

                            logger.info { "Shard $shardId is connected!" }

                            gateway.kordGateway.editPresence {
                                m.createDefaultPresence(shardId).invoke(this)
                            }

                            // We know what guilds are present in this shard
                            val guildsOnThisShard = it.data.guilds.map { it.id }.toSet()
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

                            m.cacheManager.gatewaySessions[shardId] = DeviousGatewaySession(
                                it.data.sessionId,
                                it.data.resumeGatewayUrl,
                                it.sequence ?: 0
                            )
                        }
                        is Resumed -> {
                            logger.info { "Shard $shardId resumed! alreadyTriggeredGuildReadyOnStartup? $alreadyTriggeredGuildReadyOnStartup" }

                            gateway.status.value = DeviousGateway.Status.CONNECTED

                            gateway.kordGateway.editPresence {
                                m.createDefaultPresence(shardId).invoke(this)
                            }

                            // If we already triggered the guild ready on this instance, then we wouldn't trigger it again
                            if (alreadyTriggeredGuildReadyOnStartup)
                                return@collect

                            alreadyTriggeredGuildReadyOnStartup = true

                            val guildsOnThisShard = m.cacheManager.guilds.keys.filter {
                                (it shr 22).rem(m.loritta.config.loritta.discord.maxShards).toInt() == shardId
                            }.mapNotNull { cacheManager.getGuild(Snowflake(it)) } // Should NOT be null, but if it is, then let's just pretend that it doesn't exist

                            for (guild in guildsOnThisShard) {
                                val event = GuildReadyEvent(m, gateway, guild)
                                m.forEachListeners(event, ListenerAdapter::onGuildReady)
                            }
                        }
                        is GuildCreate -> {
                            val (guildAndJoinStatus, duration) = measureTimedValue {
                                // Even if it is cached, we will create a guildAndJoinStatus entity here to update the guildAndJoinStatus on the cache
                                cacheManager.createGuild(it.guild, it.guild.channels.value)
                            }

                            // logger.info { "GuildCreate for ${guildAndJoinStatus.guild.idSnowflake} (shard $shardId) took $duration!" }

                            processGuild(guildAndJoinStatus)
                        }
                        is GuildUpdate -> {
                            // Create a guild entity here to update the guild on the cache
                            cacheManager.createGuild(it.guild, it.guild.channels.value)

                            // We won't trigger a guild create or a guild ready, because that would be too much hassle (Example: How would we check if we already fired a GuildReady?)
                        }
                        is GuildDelete -> {
                            // Ignore if it is an availability issue
                            if (it.guild.unavailable.value != null)
                                return@collect
                            
                            logger.info { "Someone removed me @ ${it.guild.id}! :(" }

                            val guild = cacheManager.getGuild(it.guild.id) ?: return@collect

                            val event = m.eventFactory.createGuildLeaveEvent(gateway, guild, it)

                            m.forEachListeners(event, ListenerAdapter::onGuildLeave)

                            m.cacheManager.deleteGuild(guild.idSnowflake)
                        }
                        is MessageCreate -> {
                            val event = m.eventFactory.createMessageReceived(gateway, it) ?: return@collect

                            m.forEachListeners(event, ListenerAdapter::onMessageReceived)
                        }
                        is MessageUpdate -> {
                            val isWebhook = DeviousUserUtils.isSenderWebhookOrSpecial(it.message)
                            val guildId = it.message.guildId.value

                            val channel = m.getChannelById(it.message.channelId)
                            if (channel == null) {
                                logger.warn { "Received message update for a channel that we don't have in our cache! Channel ID: ${it.message.channelId}; Guild ID: $guildId" }
                                return@collect
                            }
                            val guildResult = cacheManager.getGuildFailIfSnowflakeIsNotNullButGuildIsNotPresent(guildId)
                            if (guildResult is DeviousCacheManager.GuildResult.GuildNotPresent) {
                                logger.warn { "Received message update for a guild that we don't have in our cache! Channel ID: ${it.message.channelId}; Guild ID: $guildId" }
                                return@collect
                            }
                            val guild = (guildResult as? DeviousCacheManager.GuildResult.GuildPresent)?.guild

                            // The author may be null, but in this case we will ignore it
                            // (The author can be null if it was an "embed update", example: When pasting a URL in chat)
                            val authorData = it.message.author.value ?: return@collect
                            val author = cacheManager.createUser(authorData, !isWebhook)

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
                            val event = m.eventFactory.create(gateway, it) ?: return@collect

                            m.forEachListeners(event, ListenerAdapter::onMessageDelete)
                        }
                        is MessageDeleteBulk -> {
                            val event = m.eventFactory.create(gateway, it) ?: return@collect

                            m.forEachListeners(event, ListenerAdapter::onMessageBulkDelete)
                        }
                        is MessageReactionAdd -> {
                            val event = m.eventFactory.create(gateway, it) ?: return@collect

                            m.forEachListeners(event, ListenerAdapter::onGenericMessageReaction)
                        }
                        is MessageReactionRemove -> {
                            val event = m.eventFactory.create(gateway, it) ?: return@collect

                            m.forEachListeners(event, ListenerAdapter::onGenericMessageReaction)
                        }
                        is GuildMemberAdd -> {
                            val guild = cacheManager.getGuild(it.member.guildId)
                            if (guild == null) {
                                logger.warn { "Received guild member add for a guild that we don't have in our cache! Guild ID: ${it.member.guildId}" }
                                return@collect
                            }
                            val userData = it.member.user.value ?: return@collect

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
                            val member = cacheManager.createMember(user, guild, it.member)

                            val event = GuildMemberJoinEvent(m, gateway, guild, user, member)

                            m.forEachListeners(event, ListenerAdapter::onGuildMemberJoin)
                        }
                        is GuildMemberUpdate -> {
                            val guild = cacheManager.getGuild(it.member.guildId)
                            if (guild == null) {
                                logger.warn { "Received guild member update for a guild that we don't have in our cache! Guild ID: ${it.member.guildId}" }
                                return@collect
                            }
                            val userData = it.member.user

                            // Create user instance, this will store the user in cache
                            val user = cacheManager.createUser(userData, true)

                            // Create member instance, this will store the member in cache
                            val member = cacheManager.createMember(user, guild, it.member)
                        }
                        is GuildMemberRemove -> {
                            val guild = cacheManager.getGuild(it.member.guildId)
                            if (guild == null) {
                                logger.warn { "Received guild member remove for a guild that we don't have in our cache! Guild ID: ${it.member.guildId}" }
                                return@collect
                            }

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
                            val user = cacheManager.createUser(it.member.user, true)

                            // Delete the member, we don't need them anymore
                            cacheManager.deleteMember(guild, it.member.user.id)

                            val event = GuildMemberRemoveEvent(m, gateway, guild, user)

                            m.forEachListeners(event, ListenerAdapter::onGuildMemberRemove)
                        }
                        is GuildBanAdd -> {
                            val guild = cacheManager.getGuild(it.ban.guildId)
                            if (guild == null) {
                                logger.warn { "Received guild ban add for a guild that we don't have in our cache! Guild ID: ${it.ban.guildId}" }
                                return@collect
                            }

                            // Create user instance, this will store the user in cache
                            val user = cacheManager.createUser(it.ban.user, true)

                            val event = GuildBanEvent(m, gateway, guild, user)

                            m.forEachListeners(event, ListenerAdapter::onGuildBan)
                        }
                        is GuildBanRemove -> {
                            val guild = cacheManager.getGuild(it.ban.guildId)
                            if (guild == null) {
                                logger.warn { "Received guild ban remove for a guild that we don't have in our cache! Guild ID: ${it.ban.guildId}" }
                                return@collect
                            }

                            // Create user instance, this will store the user in cache
                            val user = cacheManager.createUser(it.ban.user, true)

                            val event = GuildUnbanEvent(m, gateway, guild, user)

                            m.forEachListeners(event, ListenerAdapter::onGuildUnban)
                        }
                        is GuildEmojisUpdate -> {
                            m.cacheManager.storeEmojis(it.emoji.guildId, it.emoji.emojis.map { DeviousGuildEmojiData.from(it) })
                        }
                        is GuildRoleCreate -> {
                            val role = it.role.role
                            val guild = cacheManager.getGuild(it.role.guildId)
                            if (guild == null) {
                                logger.warn { "Received guild role create for a guild that we don't have in our cache! Guild ID: ${it.role.guildId}" }
                                return@collect
                            }

                            // Create role instance, this will store the role in cache
                            cacheManager.createRole(guild, role)
                        }
                        is GuildRoleUpdate -> {
                            val role = it.role.role
                            val guild = cacheManager.getGuild(it.role.guildId)
                            if (guild == null) {
                                logger.warn { "Received guild role update for a guild that we don't have in our cache! Guild ID: ${it.role.guildId}" }
                                return@collect
                            }

                            // Create role instance, this will store the role in cache
                            cacheManager.createRole(guild, role)
                        }
                        is GuildRoleDelete -> {
                            val roleId = it.role.id
                            val guild = cacheManager.getGuild(it.role.guildId)
                            if (guild == null) {
                                logger.warn { "Received guild role delete for a guild that we don't have in our cache! Guild ID: ${it.role.guildId}" }
                                return@collect
                            }

                            // Delete role instance from cache
                            cacheManager.deleteRole(guild, roleId)
                        }
                        is ChannelCreate -> {
                            val guildId = it.channel.guildId.value ?: return@collect
                            val guild = cacheManager.getGuild(guildId)
                            if (guild == null) {
                                logger.warn { "Received channel create for a guild that we don't have in our cache! Guild ID: ${it.channel.guildId}; Channel ID: ${it.channel.id}" }
                                return@collect
                            }

                            // Create channel instance, this will store the channel in cache
                            cacheManager.createChannel(guild, it.channel)
                        }
                        is ChannelUpdate -> {
                            val guildId = it.channel.guildId.value ?: return@collect
                            val guild = cacheManager.getGuild(guildId)
                            if (guild == null) {
                                logger.warn { "Received channel update for a guild that we don't have in our cache! Guild ID: ${it.channel.guildId}; Channel ID: ${it.channel.id}" }
                                return@collect
                            }

                            // Create channel instance, this will store the channel in cache
                            cacheManager.createChannel(guild, it.channel)
                        }
                        is ChannelDelete -> {
                            val guildId = it.channel.guildId.value ?: return@collect
                            val guild = cacheManager.getGuild(guildId)
                            if (guild == null) {
                                logger.warn { "Received channel delete for a guild that we don't have in our cache! Guild ID: ${it.channel.guildId}; Channel ID: ${it.channel.id}" }
                                return@collect
                            }

                            // Delete channel, this will delete the channel from cache
                            cacheManager.deleteChannel(guild, it.channel.id)
                        }
                        is VoiceStateUpdate -> {
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
                        else -> {}
                    }
                } catch (e: Exception) {
                    logger.warn(e) { "Something went wrong while trying to process ${this::class.simpleName}" }
                }
            }
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