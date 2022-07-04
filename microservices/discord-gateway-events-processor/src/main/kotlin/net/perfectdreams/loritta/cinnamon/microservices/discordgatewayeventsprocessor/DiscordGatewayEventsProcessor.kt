package net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor

import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.gateway.Event
import dev.kord.rest.service.RestClient
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.gatewayproxy.GatewayProxy
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.AddFirstToNewChannelsModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.BomDiaECiaModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.DiscordCacheModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.StarboardModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.BomDiaECia
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.DiscordGatewayEventsProcessorTasks
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.GatewayEvent
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.KordDiscordEventUtils
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.PuddingDiscordChannelsMap
import net.perfectdreams.loritta.cinnamon.platform.utils.PuddingDiscordRolesList
import net.perfectdreams.loritta.cinnamon.platform.utils.PuddingDiscordRolesMap
import net.perfectdreams.loritta.cinnamon.platform.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuildMembers
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuilds
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import org.jetbrains.exposed.sql.and
import java.security.SecureRandom
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicInteger

class DiscordGatewayEventsProcessor(
    val config: RootConfig,
    val services: Pudding,
    val languageManager: LanguageManager,
    val replicaId: Int
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    val rest = RestClient(config.discord.token)
    val starboardModule = StarboardModule(this)
    val addFirstToNewChannelsModule = AddFirstToNewChannelsModule(this)
    val discordCacheModule = DiscordCacheModule(this)
    val bomDiaECiaModule = BomDiaECiaModule(this)

    val bomDiaECia = BomDiaECia(this)
    val random = SecureRandom()
    val activeEvents = ConcurrentLinkedQueue<Job>()
    val tasks = DiscordGatewayEventsProcessorTasks(this)

    var totalEventsProcessed = AtomicInteger()

    private val onMessageReceived: (GatewayEvent) -> (Unit) = {
        val (eventType, discordEvent) = parseEventFromString(it)

        // We will call a method that doesn't reference the "discordEventAsJsonObject" nor the "it" object, this makes it veeeery clear to the JVM that yes, you can GC the "discordEventAsJsonObject" and "it" objects
        // (Will it really GC the object? idk, but I hope it will)
        launchEventProcessorJob(eventType, discordEvent)

        totalEventsProcessed.incrementAndGet()
    }

    private val gatewayProxies = config.gatewayProxies.filter { it.replicaId == replicaId }.map {
        GatewayProxy(it.url, it.authorizationToken, onMessageReceived)
    }

    fun start() {
        gatewayProxies.forEachIndexed { index, gatewayProxy ->
            logger.info { "Starting Gateway Proxy $index" }
            gatewayProxy.start()
        }

        tasks.start()

        // bomDiaECia.startBomDiaECiaTask()
    }

    suspend fun getPermissions(guildId: Snowflake, channelId: Snowflake, userId: Snowflake): Permissions {
        // Create an empty permissions object
        var permissions = Permissions()

        services.transaction {
            val userRoleIds = DiscordGuildMembers
                .slice(DiscordGuilds.roles)
                .selectFirstOrNull { DiscordGuildMembers.guildId eq guildId.toLong() and (DiscordGuildMembers.userId eq userId.toLong()) }
                ?.get(DiscordGuildMembers.roles)
                ?.let {
                    Json.decodeFromString<PuddingDiscordRolesList>(it)
                } ?: emptyList()

            val guild = DiscordGuilds
                .slice(DiscordGuilds.roles, DiscordGuilds.channels)
                .selectFirstOrNull { DiscordGuilds.id eq guildId.toLong() }

            val rolesAsJson = guild?.get(DiscordGuilds.roles)
            val channelsAsJson = guild?.get(DiscordGuilds.channels)

            val guildRoles = rolesAsJson?.let { Json.decodeFromString<PuddingDiscordRolesMap>(it) } ?: emptyMap()
            val guildChannels = channelsAsJson?.let { Json.decodeFromString<PuddingDiscordChannelsMap>(it) } ?: emptyMap()

            val guildChannel = guildChannels[channelId.toString()]
            val everyoneRole = guildRoles[guildId.toString()]

            val userRoles = guildRoles
                .filter { it.key in userRoleIds }
                .values
                .toMutableList()

            if (everyoneRole != null) {
                userRoles.add(everyoneRole)
            } else {
                logger.warn { "Everyone role is null in $guildId! We will ignore it..." }
            }

            // Sort all roles by position...
            userRoles.sortedBy { it.position }
                .forEach {
                    // Keep "plus"'ing the permissions!
                    permissions = permissions.plus(it.permissions)
                }

            val entityIds = mutableSetOf(userId)
            entityIds.addAll(userRoleIds.map { Snowflake(it) })

            // Now we will get permission overrides
            val permissionOverrides = guildChannel?.permissionOverwrites?.value

            // TODO: I'm not sure if that's how permission overrides are actually calculated
            permissionOverrides?.forEach {
                permissions = permissions.plus(it.allow)
                permissions = permissions.minus(it.deny)
            }
        }

        return permissions
    }

    private fun parseEventFromString(discordGatewayEvent: GatewayEvent): Pair<String, Event?> {
        val discordEventAsJsonObject = Json.parseToJsonElement(discordGatewayEvent.jsonAsString ?: error("Trying to parse an already parsed/null GatewayEvent!")).jsonObject
        discordGatewayEvent.jsonAsString = null
        val eventType = discordEventAsJsonObject["t"]?.jsonPrimitive?.content ?: "UNKNOWN"
        val discordEvent = KordDiscordEventUtils.parseEventFromJsonObject(discordEventAsJsonObject)

        return Pair(eventType, discordEvent)
    }

    private fun launchEventProcessorJob(type: String, discordEvent: Event?) {
        if (discordEvent != null)
            launchEventProcessorJob(discordEvent)
        else
            logger.warn { "Unknown Discord event received $type! We are going to ignore the event... kthxbye!" }
    }

    private fun launchEventProcessorJob(discordEvent: Event) {
        val coroutineName = "Event ${discordEvent::class.simpleName}"
        launchEventJob(coroutineName) {
            try {
                discordCacheModule.processEvent(discordEvent)
                addFirstToNewChannelsModule.processEvent(discordEvent)
                starboardModule.processEvent(discordEvent)
                // bomDiaECiaModule.processEvent(discordEvent)
            } catch (e: Throwable) {
                logger.warn(e) { "Something went wrong while trying to process $coroutineName! We are going to ignore..." }
            }
        }
    }

    private fun launchEventJob(coroutineName: String, block: suspend CoroutineScope.() -> Unit) {
        val start = System.currentTimeMillis()
        val job = GlobalScope.launch(
            CoroutineName(coroutineName),
            block = block
        )

        activeEvents.add(job)

        // Yes, the order matters, since sometimes the invokeOnCompletion would be invoked before the job was
        // added to the list, causing leaks.
        // invokeOnCompletion is also invoked even if the job was already completed at that point, so no worries!
        job.invokeOnCompletion {
            activeEvents.remove(job)

            val diff = System.currentTimeMillis() - start
            if (diff >= 60_000) {
                logger.warn { "Coroutine $job ($coroutineName) took too long to process! ${diff}ms" }
            }
        }
    }
}