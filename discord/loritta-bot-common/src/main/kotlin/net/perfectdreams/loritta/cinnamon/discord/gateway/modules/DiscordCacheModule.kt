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
    // TODO - DeviousFun: Clean up this, because the cache has been moved somewhere else
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
                            val gatewayProxy = m.gatewayManager.getGatewayForGuild(guildId)
                            gatewayProxy.kordGateway.send(
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

                            // TODO - DeviousFun
                            // createOrUpdateGuildMember(guildId, event.message.author.id, member)
                        }
                    }
                }
            }
            else -> {}
        }
        return ModuleResult.Continue
    }
}