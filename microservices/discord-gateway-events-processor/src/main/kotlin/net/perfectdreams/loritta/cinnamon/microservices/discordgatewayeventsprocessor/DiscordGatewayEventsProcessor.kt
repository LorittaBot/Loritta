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
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.common.locale.LanguageManager
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.AddFirstToNewChannelsModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.BomDiaECiaModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.DiscordCacheModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.modules.StarboardModule
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.tables.DiscordGatewayEvents
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.BomDiaECia
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.DiscordGatewayEventsProcessorTasks
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.QueueDatabase
import net.perfectdreams.loritta.cinnamon.microservices.discordgatewayeventsprocessor.utils.config.RootConfig
import net.perfectdreams.loritta.cinnamon.platform.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuildChannelPermissionOverrides
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuildMemberRoles
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuildRoles
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.security.SecureRandom
import java.util.concurrent.ConcurrentLinkedQueue

class DiscordGatewayEventsProcessor(
    val config: RootConfig,
    val services: Pudding,
    val queueDatabase: QueueDatabase,
    val languageManager: LanguageManager
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

    fun start() {
        runBlocking {
            transaction(queueDatabase.database) {
                SchemaUtils.createMissingTablesAndColumns(
                    DiscordGatewayEvents
                )
            }
        }

        tasks.start()

        bomDiaECia.startBomDiaECiaTask()
    }

    suspend fun getPermissions(guildId: Snowflake, channelId: Snowflake, userId: Snowflake): Permissions {
        // Create an empty permissions object
        var permissions = Permissions()

        services.transaction {
            val userRoles = DiscordGuildMemberRoles.select {
                DiscordGuildMemberRoles.guildId eq guildId.toLong() and
                        (DiscordGuildMemberRoles.userId eq userId.toLong())
            }.toList()

            val roles = DiscordGuildRoles.select {
                DiscordGuildRoles.roleId inList userRoles.map {
                    it[DiscordGuildMemberRoles.roleId]
                } and (DiscordGuildRoles.guildId eq guildId.toLong())
            }.toList()

            // Sort all roles by position...
            roles.sortedBy { it[DiscordGuildRoles.position] }
                .forEach {
                    // Keep "plus"'ing the permissions!
                    permissions = permissions.plus(Permissions(it[DiscordGuildRoles.permissions].toString()))
                }

            val entityIds = mutableSetOf(userId.toLong())
            entityIds.addAll(roles.map { it[DiscordGuildRoles.roleId] })

            // Now we will get permission overrides
            val permissionOverrides = DiscordGuildChannelPermissionOverrides.select {
                DiscordGuildChannelPermissionOverrides.guildId eq guildId.toLong() and
                        (DiscordGuildChannelPermissionOverrides.channelId eq channelId.toLong()) and
                        (DiscordGuildChannelPermissionOverrides.entityId inList entityIds)
            }.toList()

            // TODO: I'm not sure if that's how permission overrides are actually calculated
            permissionOverrides.forEach {
                permissions = permissions.plus(Permissions(it[DiscordGuildChannelPermissionOverrides.allow].toString()))
                permissions = permissions.minus(Permissions(it[DiscordGuildChannelPermissionOverrides.deny].toString()))
            }
        }

        return permissions
    }

    fun launchEventProcessorJob(discordEvent: Event) {
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