package net.perfectdreams.loritta.cinnamon.discord.gateway.modules

import com.github.benmanes.caffeine.cache.Caffeine
import dev.kord.common.entity.*
import dev.kord.common.entity.optional.value
import dev.kord.gateway.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.sync.withPermit
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.gateway.GatewayEventContext
import net.perfectdreams.loritta.cinnamon.discord.utils.entitycache.PuddingGuildMember
import net.perfectdreams.loritta.cinnamon.discord.utils.entitycache.PuddingGuildVoiceState
import net.perfectdreams.loritta.cinnamon.discord.utils.entitycache.ZstdDictionaries
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.hsetByteArray
import net.perfectdreams.loritta.cinnamon.discord.utils.redis.hsetByteArrayOrDelIfMapIsEmpty
import java.util.concurrent.TimeUnit

class DiscordCacheModule(private val m: LorittaBot) : ProcessDiscordEventsModule() {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Used to avoid updating the same information at the same time, causing "Could not serialize access due to concurrent update"
     */
    private val mutexes = Caffeine.newBuilder()
        .expireAfterAccess(1L, TimeUnit.MINUTES)
        .build<String, Mutex>()
        .asMap()

    /**
     * Used to avoid sending a transaction on a MessageCreate event just to update the user's roles
     */
    private val userRolesHashes = Caffeine.newBuilder()
        .expireAfterAccess(1L, TimeUnit.MINUTES)
        .build<Snowflake, Int>()
        .asMap()

    /**
     * To avoid all connections in the connection pool being used by GuildCreate events, we will limit to max `connections in the pool - 5`, with a minimum of one permit GuildCreate in parallel
     *
     * This avoids issues where all events stop being processed due to a "explosion" of GuildCreates after a shard restart!
     */
    private val guildCreateSemaphore = Semaphore((m.jedisPool.maxTotal - 5).coerceAtLeast(1))

    private suspend inline fun withMutex(vararg ids: Snowflake, action: () -> Unit) = mutexes.getOrPut(ids.joinToString(":")) { Mutex() }.withLock(action = action)

    override suspend fun processEvent(context: GatewayEventContext): ModuleResult {
        when (val event = context.event) {
            is GuildCreate -> {
                // logger.info { "Howdy ${event.guild.id} (${event.guild.name})! Is unavailable? ${event.guild.unavailable}" }

                if (!event.guild.unavailable.discordBoolean) {
                    val start = System.currentTimeMillis()

                    val guildId = event.guild.id
                    val guildName = event.guild.name
                    val guildIcon = event.guild.icon
                    val guildOwnerId = event.guild.ownerId
                    val guildRoles = event.guild.roles
                    val guildChannels = event.guild.channels.value!! // Shouldn't be null in a GUILD_CREATE event
                    val guildEmojis = event.guild.emojis
                    // If your bot does not have the GUILD_PRESENCES Gateway Intent, or if the guild has over 75k members, members and presences returned in this event will only contain your bot and users in voice channels.
                    val guildMembers = event.guild.members.value ?: emptyList()
                    val guildVoiceStates = event.guild.voiceStates.value ?: emptyList()

                    val lorittaVoiceState = guildVoiceStates.firstOrNull { it.userId == m.config.loritta.discord.applicationId }
                    if (lorittaVoiceState != null) {
                        // Wait... that's us! omg omg omg
                        val lorittaVoiceConnection = m.voiceConnectionsManager.voiceConnections[guildId]

                        if (lorittaVoiceConnection == null) {
                            // B-but... we shouldn't be here!? Uhhh, meow? Time to disconnect maybe??
                            logger.warn { "Looks like we are connected @ $guildId but we don't have any active voice connections here! We will disconnect from the voice channel to avoid issues..." }
                            val gatewayProxy = m.lorittaShards.gatewayManager.getGatewayForGuild(guildId)
                            gatewayProxy.send(
                                UpdateVoiceStatus(
                                    guildId,
                                    null,
                                    selfMute = false,
                                    selfDeaf = false
                                )
                            )
                        }
                    }

                    withMutex(guildId) {
                        guildCreateSemaphore.withPermit {
                            // We are going to do this within a transaction because this needs to be an "atomic" change
                            // Also because Jedis transactions are pipelined, which improves performance (yay)
                            m.redisTransaction {
                                m.cache.createOrUpdateGuild(
                                    it,
                                    guildId,
                                    guildName,
                                    guildIcon,
                                    guildOwnerId,
                                    guildRoles,
                                    guildChannels,
                                    guildEmojis
                                )

                                for (member in guildMembers) {
                                    it.hsetByteArray(
                                        m.redisKeys.discordGuildMembers(guildId),
                                        member.user.value!!.id.toString(),
                                        m.cache.encodeToBinary(
                                            PuddingGuildMember(
                                                member.user.value!!.id,
                                                member.roles
                                            ),
                                            ZstdDictionaries.Dictionary.NO_DICTIONARY
                                        )
                                    )
                                }

                                // Reinsert them
                                it.hsetByteArrayOrDelIfMapIsEmpty(
                                    m.redisKeys.discordGuildVoiceStates(guildId),
                                    guildVoiceStates.associate {
                                        it.userId.toString() to m.cache.encodeToBinary(
                                            PuddingGuildVoiceState(
                                                it.channelId!!, // Shouldn't be null because they are in a channel
                                                it.userId
                                            ),
                                            ZstdDictionaries.Dictionary.NO_DICTIONARY
                                        )
                                    }
                                )
                            }
                        }
                    }

                    logger.info { "GuildCreate for $guildId took ${System.currentTimeMillis() - start}ms" }
                }
            }
            is GuildUpdate -> {
                if (!event.guild.unavailable.discordBoolean) {
                    val start = System.currentTimeMillis()

                    val guildId = event.guild.id
                    val guildName = event.guild.name
                    val guildIcon = event.guild.icon
                    val guildOwnerId = event.guild.ownerId
                    val guildRoles = event.guild.roles
                    val guildChannels = event.guild.channels.value
                    val guildEmojis = event.guild.emojis

                    withMutex(guildId) {
                        m.redisTransaction {
                            m.cache.createOrUpdateGuild(
                                it,
                                guildId,
                                guildName,
                                guildIcon,
                                guildOwnerId,
                                guildRoles,
                                guildChannels,
                                guildEmojis
                            )
                        }
                    }

                    logger.info { "GuildUpdate for $guildId took ${System.currentTimeMillis() - start}ms" }
                }
            }
            is GuildEmojisUpdate -> {
                val guildId = event.emoji.guildId
                val discordEmojis = event.emoji.emojis

                withMutex(guildId) {
                    m.cache.updateGuildEmojis(guildId, discordEmojis)
                }
            }
            is MessageCreate -> {
                val guildId = event.message.guildId.value
                val member = event.message.member.value

                if (guildId != null && member != null) {
                    // To avoid unnecessary database updates just because someone sent a message in chat, we will store a hash of their roles in memory
                    // If the role list hash doesn't match, *then* we send a transaction updating the user's information
                    val userRoleHash = userRolesHashes[event.message.author.id]

                    if (userRoleHash != member.roles.hashCode()) {
                        withMutex(guildId, event.message.author.id) {
                            userRolesHashes[event.message.author.id] = member.roles.hashCode()

                            createOrUpdateGuildMember(guildId, event.message.author.id, member)
                        }
                    }
                }
            }
            is GuildMemberAdd -> {
                withMutex(event.member.guildId, event.member.user.value!!.id) {
                    createOrUpdateGuildMember(event.member)
                }
            }
            is GuildMemberUpdate -> {
                withMutex(event.member.guildId, event.member.user.id) {
                    createOrUpdateGuildMember(event.member)
                }
            }
            is GuildMemberRemove -> {
                withMutex(event.member.guildId, event.member.user.id) {
                    deleteGuildMember(event.member)
                }
            }
            is ChannelCreate -> {
                val guildId = event.channel.guildId.value
                if (guildId != null)
                    withMutex(guildId, event.channel.id) {
                        createOrUpdateGuildChannel(guildId, event.channel)
                    }
            }
            is ChannelUpdate -> {
                val guildId = event.channel.guildId.value
                if (guildId != null)
                    withMutex(guildId, event.channel.id) {
                        createOrUpdateGuildChannel(guildId, event.channel)
                    }
            }
            is ChannelDelete -> {
                val guildId = event.channel.guildId.value
                if (guildId != null)
                    withMutex(guildId, event.channel.id) {
                        deleteGuildChannel(guildId, event.channel)
                    }
            }
            is GuildRoleCreate -> {
                withMutex(event.role.guildId, event.role.role.id) {
                    createOrUpdateRole(event.role.guildId, event.role.role)
                }
            }
            is GuildRoleUpdate -> {
                withMutex(event.role.guildId, event.role.role.id) {
                    createOrUpdateRole(event.role.guildId, event.role.role)
                }
            }
            is GuildRoleDelete -> {
                withMutex(event.role.guildId, event.role.id) {
                    deleteRole(event.role.guildId, event.role.id)
                }
            }
            is GuildDelete -> {
                // If the unavailable field is not set, the user/bot was removed from the guild.
                if (event.guild.unavailable.value == null) {
                    logger.info { "Someone removed me @ ${event.guild.id}! :(" }
                    withMutex(event.guild.id) {
                        removeGuildData(event.guild.id)
                    }
                }
            }
            is VoiceStateUpdate -> {
                val guildId = event.voiceState.guildId.value!! // Shouldn't be null here
                val channelId = event.voiceState.channelId
                val userId = event.voiceState.userId

                if (userId == m.config.loritta.discord.applicationId) {
                    // Wait... that's us! omg omg omg
                    val lorittaVoiceConnection = m.voiceConnectionsManager.voiceConnections[guildId]
                    if (lorittaVoiceConnection != null) {
                        if (channelId != null) {
                            logger.info { "We were moved @ $guildId to $channelId, updating our voice connection to match it..." }
                            lorittaVoiceConnection.channelId = channelId
                        } else {
                            logger.info { "We were removed from a voice channel @ $guildId, shutting down our voice connection..." }
                            m.voiceConnectionsManager.shutdownVoiceConnection(guildId, lorittaVoiceConnection)
                        }
                    }
                }

                withMutex(guildId) {
                    val voiceState = event.voiceState

                    m.redisConnection {
                        // Channel is null, so let's delete voice states in the guild related to the user
                        if (channelId == null) {
                            it.hdel(m.redisKeys.discordGuildVoiceStates(guildId), voiceState.userId.toString())
                        } else {
                            // Channel is not null, let's upsert
                            it.hsetByteArray(
                                m.redisKeys.discordGuildVoiceStates(guildId),
                                userId.toString(),
                                m.cache.encodeToBinary(
                                    PuddingGuildVoiceState(
                                        channelId,
                                        userId
                                    ),
                                    ZstdDictionaries.Dictionary.NO_DICTIONARY
                                )
                            )
                        }
                    }
                }
            }
            else -> {}
        }
        return ModuleResult.Continue
    }

    private suspend fun createOrUpdateGuildMember(guildMember: DiscordAddedGuildMember) {
        createOrUpdateGuildMember(
            guildMember.guildId,
            guildMember.user.value!!.id,
            guildMember.roles
        )
    }

    private suspend fun createOrUpdateGuildMember(guildMember: DiscordUpdatedGuildMember) {
        createOrUpdateGuildMember(
            guildMember.guildId,
            guildMember.user.id,
            guildMember.roles
        )
    }

    private suspend fun createOrUpdateGuildMember(guildId: Snowflake, userId: Snowflake, guildMember: DiscordGuildMember) {
        createOrUpdateGuildMember(
            guildId,
            userId,
            guildMember.roles
        )
    }

    private suspend fun createOrUpdateGuildMember(
        guildId: Snowflake,
        userId: Snowflake,
        roles: List<Snowflake>
    ) {
        m.redisConnection {
            it.hsetByteArray(
                m.redisKeys.discordGuildMembers(guildId),
                userId.toString(),
                m.cache.encodeToBinary(
                    PuddingGuildMember(
                        userId,
                        roles
                    ),
                    ZstdDictionaries.Dictionary.NO_DICTIONARY
                )
            )
        }
    }

    private suspend fun deleteGuildMember(guildMember: DiscordRemovedGuildMember) {
        m.redisConnection {
            it.hdel(
                m.redisKeys.discordGuildMembers(guildMember.guildId),
                guildMember.user.id.toString()
            )
        }
    }

    private suspend fun createOrUpdateGuildChannel(guildId: Snowflake, channel: DiscordChannel) {
        m.redisConnection {
            it.hsetByteArray(
                m.redisKeys.discordGuildChannels(guildId),
                channel.id.toString(),
                m.cache.encodeToBinary(channel, ZstdDictionaries.Dictionary.CHANNELS_V1)
            )
        }
    }

    private suspend fun deleteGuildChannel(guildId: Snowflake, channel: DiscordChannel) {
        m.redisConnection {
            it.hdel(
                m.redisKeys.discordGuildChannels(guildId),
                channel.id.toString()
            )
        }
    }

    private suspend fun createOrUpdateRole(guildId: Snowflake, role: DiscordRole) {
        m.redisConnection {
            it.hsetByteArray(
                m.redisKeys.discordGuildRoles(guildId),
                role.id.toString(),
                m.cache.encodeToBinary(role, ZstdDictionaries.Dictionary.ROLES_V1)
            )
        }
    }

    private suspend fun deleteRole(guildId: Snowflake, roleId: Snowflake) {
        m.redisConnection {
            it.hdel(
                m.redisKeys.discordGuildRoles(guildId),
                roleId.toString()
            )
        }
    }

    private suspend fun removeGuildData(guildId: Snowflake) {
        logger.info { "Removing $guildId's cached data..." }

        m.redisTransaction {
            it.del(m.redisKeys.discordGuilds(guildId))
            it.del(m.redisKeys.discordGuildMembers(guildId))
            it.del(m.redisKeys.discordGuildRoles(guildId))
            it.del(m.redisKeys.discordGuildChannels(guildId))
            it.del(m.redisKeys.discordGuildEmojis(guildId))
            it.del(m.redisKeys.discordGuildVoiceStates(guildId))
        }
    }
}