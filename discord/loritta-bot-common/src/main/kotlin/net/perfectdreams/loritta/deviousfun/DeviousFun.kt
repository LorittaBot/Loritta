package net.perfectdreams.loritta.deviousfun

import dev.kord.common.entity.Snowflake
import dev.kord.rest.json.JsonErrorCode
import dev.kord.rest.request.KtorRequestException
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import mu.KotlinLogging
import net.perfectdreams.loritta.deviousfun.cache.DeviousCacheManager
import net.perfectdreams.loritta.deviousfun.entities.*
import net.perfectdreams.loritta.deviousfun.events.DeviousEventFactory
import net.perfectdreams.loritta.deviousfun.events.Event
import net.perfectdreams.loritta.deviousfun.gateway.GatewayManager
import net.perfectdreams.loritta.deviousfun.hooks.ListenerAdapter
import net.perfectdreams.loritta.morenitta.LorittaBot
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KFunction2

/**
 * If it looks like JDA, swims like JDA, and quacks like JDA, then it probably is a JDA instance.
 *
 * But in reality it is **Devious Fun**!
 *
 * Devious Fun has classes that mimicks JDA classes to give sort-of source compatibility, because rewriting Loritta would take
 * too much time and would be too much work.
 *
 * The issue with JDA is that it is VERY good for smol bots, but for big bots it falls short because you can't use...
 * * Cache in Redis
 * * Resuming gateway connections after a restart (because JDA requires everything to be in cache)
 * So on and so forth...
 *
 * **Method conventions:**
 * * Properties: Cached in the class itself
 * * `get`: Will be retrieved from Redis, null if the entity doesn't exist.
 * * `retrieve`: Will be retrieved from Redis, or from Discord's API if not present. Throws exception if the entity does not exist.
 */
class DeviousFun(val loritta: LorittaBot) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    // TODO: Mutex locking based on the entity ID
    val eventFactory = DeviousEventFactory(this)
    val listeners = mutableListOf<ListenerAdapter>()
    val cacheManager = DeviousCacheManager(this)
    val entityLocks = ConcurrentHashMap<Snowflake, Mutex>()
    val gatewayManager = GatewayManager(
        this,
        loritta.config.loritta.discord.token,
        loritta.lorittaCluster.minShard,
        loritta.lorittaCluster.maxShard,
        loritta.config.loritta.discord.maxShards
    )

    /**
     * To avoid all connections in the connection pool being used by GuildCreate events, we will limit to max `connections in the pool - 5`, with a minimum of one permit GuildCreate in parallel
     *
     * This avoids issues where all events stop being processed due to a "explosion" of GuildCreates after a shard restart!
     */
    val guildCreateSemaphore = Semaphore((loritta.jedisPool.maxTotal - 5).coerceAtLeast(1))

    fun registerListeners(vararg listeners: ListenerAdapter) {
        this.listeners.addAll(listeners)
    }

    suspend fun getChannelById(id: Long) = getChannelById(Snowflake(id))

    suspend fun getChannelById(id: Snowflake): Channel? {
        return cacheManager.getChannel(id)
    }

    suspend fun retrieveSelfUser() = retrieveUserById(loritta.config.loritta.discord.applicationId)

    suspend fun getUserById(id: Snowflake): User? {
        return cacheManager.getUser(id)
    }

    suspend fun getMemberById(guild: Guild, id: Snowflake): Member? {
        // Unknown user, bail out
        val user = cacheManager.getUser(id) ?: return null

        return getMemberByUser(guild, user)
    }

    suspend fun getMemberByUser(guild: Guild, user: User): Member? {
        return cacheManager.getMember(user, guild)
    }

    suspend fun getGuildById(id: String) = cacheManager.getGuild(Snowflake(id))
    suspend fun getGuildById(id: Long) = cacheManager.getGuild(Snowflake(id))

    suspend fun getGuildById(id: Snowflake): Guild? {
        return cacheManager.getGuild(id)
    }

    suspend fun retrieveUserById(id: Snowflake): User {
        val cachedUser = cacheManager.getUser(id)
        if (cachedUser != null)
            return cachedUser

        addContextToException({ "Something went wrong while trying to query user $id" }) {
            return cacheManager.createUser(loritta.rest.user.getUser(id), true)
        }
    }

    suspend fun retrieveUserOrNullById(id: Snowflake): User? {
        val cachedUser = cacheManager.getUser(id)
        if (cachedUser != null)
            return cachedUser

        return try {
            cacheManager.createUser(loritta.rest.user.getUser(id), true)
        } catch (e: KtorRequestException) {
            return null
        }
    }

    suspend fun retrieveMemberById(guild: Guild, id: Snowflake): Member {
        val cachedUser = retrieveUserById(id)

        val cachedMember = getMemberByUser(guild, cachedUser)
        if (cachedMember != null)
            return cachedMember

        addContextToException({ "Something went wrong while trying to query member $id in guild ${guild.idSnowflake}" }) {
            val member = loritta.rest.guild.getGuildMember(guild.idSnowflake, id)

            return cacheManager.createMember(
                cachedUser,
                guild,
                member
            )
        }
    }

    suspend fun retrieveGuildById(id: Snowflake): Guild {
        val cachedGuild = getGuildById(id)
        if (cachedGuild != null)
            return cachedGuild

        addContextToException({ "Something went wrong while trying to query guild $id" }) {
            val guild = loritta.rest.guild.getGuild(id, withCounts = true)
            val channels = loritta.rest.guild.getGuildChannels(id)
            return cacheManager.createGuild(guild, channels)
        }
    }

    suspend fun retrieveGuildOrNullById(id: Snowflake): Guild? {
        try {
            return retrieveGuildById(id)
        } catch (e: KtorRequestException) {
            if (e.error?.code == JsonErrorCode.MissingAccess)
                return null

            throw e
        }
    }

    suspend fun retrieveChannelById(id: Snowflake): Channel {
        val cachedChannel = getChannelById(id)
        if (cachedChannel != null)
            return cachedChannel

        addContextToException({ "Something went wrong while trying to query channel $id" }) {
            val channel = loritta.rest.channel.getChannel(id)
            val guild = channel.guildId.value?.let { retrieveGuildById(it) }
            return cacheManager.createChannel(guild, channel)
        }
    }

    suspend fun retrieveChannelById(guild: Guild, id: Snowflake): Channel {
        val cachedChannel = getChannelById(id)
        if (cachedChannel != null)
            return cachedChannel

        addContextToException({ "Something went wrong while trying to query channel $id in guild ${guild.idSnowflake}" }) {
            val channel = loritta.rest.channel.getChannel(id)
            return cacheManager.createChannel(guild, channel)
        }
    }

    /**
     * Gets how many guilds are cached
     */
    suspend fun getGuildCount() = loritta.redisConnection {
        it.hlen(loritta.redisKeys.discordGuilds())
    }

    /**
     * Invokes every [ListenerAdapter]'s [method] with [event]
     *
     * [Throwable]s are catched and logged, but won't halt subsequent listeners.
     */
    fun <T : Event> forEachListeners(event: T, method: KFunction2<ListenerAdapter, T, Unit>) {
        for (listener in listeners) {
            try {
                method.invoke(listener, event)
            } catch (e: Throwable) {
                logger.warn(e) { "Something went wrong while sending ${method.name} to $listener!" }
            }
        }
    }

    private inline fun <T> addContextToException(message: () -> (String), action: () -> (T)): T {
        try {
            return action.invoke()
        } catch (e: KtorRequestException) {
            val exception = FakeExceptionForContextException(message.invoke())
            e.addSuppressed(exception)
            throw e
        }
    }


    private class FakeExceptionForContextException(override val message: String) : Exception()
}