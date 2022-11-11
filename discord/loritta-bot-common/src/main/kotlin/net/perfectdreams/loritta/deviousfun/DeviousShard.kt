package net.perfectdreams.loritta.deviousfun

import dev.kord.common.entity.PresenceStatus
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.builder.PresenceBuilder
import dev.kord.rest.json.JsonErrorCode
import dev.kord.rest.request.KtorRequestException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import mu.KotlinLogging
import net.perfectdreams.loritta.deviouscache.data.*
import net.perfectdreams.loritta.deviousfun.cache.DeviousCacheManager
import net.perfectdreams.loritta.deviousfun.entities.*
import net.perfectdreams.loritta.deviousfun.events.DeviousEventFactory
import net.perfectdreams.loritta.deviousfun.events.Event
import net.perfectdreams.loritta.deviousfun.gateway.DeviousGateway
import net.perfectdreams.loritta.deviousfun.hooks.ListenerAdapter
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.config.LorittaConfig
import java.util.*
import kotlin.reflect.KFunction2

/**
 * If it looks like JDA, swims like JDA, and quacks like JDA, then it probably is a JDA instance.
 *
 * But in reality it is **Devious Fun**!
 *
 * Devious Fun has classes that mimicks JDA classes to give sort-of source compatibility, because rewriting Loritta would take
 * too much time and would be too much work.
 *
 * The issue with JDA is that it is VERY good for smol bots, but for big bots it falls short because you can't...
 * * Cache on disk (for gateway resumes)
 * * Resuming gateway connections after a restart (because JDA requires everything to be in cache)
 * * Can't filter what fields are cached
 * So on and so forth...
 *
 * **Method conventions:**
 * * Properties: Cached in the class itself
 * * `get`: Will be retrieved from memory, null if the entity doesn't exist.
 * * `retrieve`: Will be retrieved from memory, or from Discord's API if not present. Throws exception if the entity does not exist.
 */
class DeviousShard(
    val loritta: LorittaBot,
    val deviousGateway: DeviousGateway,
    val triggeredEventsDueToCacheUpdate: kotlinx.coroutines.channels.Channel<(DeviousShard) -> (Unit)>
) {
    companion object {
        private val logger = KotlinLogging.logger {}

        fun createActivityTextWithShardAndClusterId(activityText: String, lorittaCluster: LorittaConfig.LorittaClustersConfig.LorittaClusterConfig, shardId: Int) = "$activityText | Cluster ${lorittaCluster.id} [$shardId]"
    }

    val shardId
        get() = deviousGateway.shardId


    val cacheManagerDoNotUseThisUnlessIfYouKnowWhatYouAreDoing: MutableStateFlow<DeviousCacheManager?> = MutableStateFlow(null)
    val cacheManagerOrNull
        get() = cacheManagerDoNotUseThisUnlessIfYouKnowWhatYouAreDoing.value
    val eventFactory = DeviousEventFactory(this)
    val listeners = mutableListOf<ListenerAdapter>()

    /**
     * Queued guild events that must be executed after the guild is up
     */
    val queuedGuildEvents = mutableMapOf<Snowflake, LinkedList<dev.kord.gateway.Event>>()

    val guildsOnThisShard = mutableSetOf<Snowflake>()
    val unavailableGuilds = mutableSetOf<Snowflake>()

    /**
     * Gets the [DeviousCacheManager], and suspends if the [DeviousCacheManager] is not set yet
     */
    suspend fun getCacheManager() = cacheManagerDoNotUseThisUnlessIfYouKnowWhatYouAreDoing
        .filterNotNull().first()

    fun registerListeners(vararg listeners: ListenerAdapter) {
        this.listeners.addAll(listeners)
    }

    suspend fun getChannelById(id: Long) = getChannelById(Snowflake(id))

    suspend fun getChannelById(id: Snowflake): Channel? {
        return getCacheManager().getChannel(id)
    }

    suspend fun retrieveSelfUser() = retrieveUserById(loritta.config.loritta.discord.applicationId)

    suspend fun getUserById(id: Snowflake): User? {
        return getCacheManager().getUser(id)
    }

    suspend fun getMemberById(guild: Guild, id: Snowflake): Member? {
        // Unknown user, bail out
        val user = getCacheManager().getUser(id) ?: return null

        return getMemberByUser(guild, user)
    }

    suspend fun getMemberByUser(guild: Guild, user: User): Member? {
        return getCacheManager().getMember(user, guild)
    }

    suspend fun getGuildById(id: String) = getCacheManager().getGuild(Snowflake(id))
    suspend fun getGuildById(id: Long) = getCacheManager().getGuild(Snowflake(id))

    suspend fun getGuildById(id: Snowflake): Guild? {
        return getCacheManager().getGuild(id)
    }

    suspend fun retrieveUserById(id: Snowflake): User {
        val cachedUser = getCacheManager().getUser(id)
        if (cachedUser != null)
            return cachedUser

        addContextToException({ "Something went wrong while trying to query user $id" }) {
            return getCacheManager().createUser(loritta.rest.user.getUser(id), true)
        }
    }

    suspend fun retrieveUserOrNullById(id: Snowflake): User? {
        val cachedUser = getCacheManager().getUser(id)
        if (cachedUser != null)
            return cachedUser

        return try {
            getCacheManager().createUser(loritta.rest.user.getUser(id), true)
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

            return getCacheManager().createMember(
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
            return getCacheManager().createGuild(guild, channels).guild
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

            // If the guild is null, fallback to a non cached channel instance
            val guild = channel.guildId.value?.let { retrieveGuildById(it) }
                ?: return Channel(this, null, DeviousChannelData.from(null, channel))


            return getCacheManager().createChannel(guild, channel)
        }
    }

    suspend fun retrieveChannelById(guild: Guild, id: Snowflake): Channel {
        val cachedChannel = getChannelById(id)
        if (cachedChannel != null)
            return cachedChannel

        addContextToException({ "Something went wrong while trying to query channel $id in guild ${guild.idSnowflake}" }) {
            val channel = loritta.rest.channel.getChannel(id)
            return getCacheManager().createChannel(guild, channel)
        }
    }

    suspend fun getMutualGuilds(user: User): List<Guild> {
        val lightweightSnowflake = user.idSnowflake.toLightweightSnowflake()
        val mutualGuildIds = mutableSetOf<LightweightSnowflake>()
        getCacheManager().members.forEach { guildId, members ->
            if (members.containsKey(lightweightSnowflake))
                mutualGuildIds.add(guildId)
        }

        return mutualGuildIds.mapNotNull { getGuildById(it.toKordSnowflake()) }
    }

    /**
     * Gets how many guilds are cached
     */
    suspend fun getGuildCount() = getCacheManager().guilds.size

    /**
     * Invokes every [ListenerAdapter]'s [method] with [event]
     *
     * [Throwable]s are catched and logged, but won't halt subsequent listeners.
     */
    fun <T : Event> forEachListeners(event: T, method: KFunction2<ListenerAdapter, T, Unit>) {
        // Ignore if it is passive mode
        if (loritta.passiveMode)
            return

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

    fun createActivityTextWithShardAndClusterId(activityText: String) = createActivityTextWithShardAndClusterId(activityText, loritta.lorittaCluster, shardId)

    private class FakeExceptionForContextException(override val message: String) : Exception()
}