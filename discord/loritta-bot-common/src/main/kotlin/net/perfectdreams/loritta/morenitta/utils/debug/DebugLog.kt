package net.perfectdreams.loritta.morenitta.utils.debug

import net.perfectdreams.loritta.morenitta.listeners.EventLogListener
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import java.util.concurrent.ThreadPoolExecutor

object DebugLog {
    private val logger = KotlinLogging.logger {}
    var cancelAllEvents = false
        get() {
            if (field)
                logger.warn { "All received events are cancelled and ignored!" }
            return field
        }

    fun showExtendedInfo(loritta: LorittaBot) {
        val mb = 1024 * 1024
        val runtime = Runtime.getRuntime()

        logger.info("===[ EXTENDED INFO ]===")
        logger.info("Used Memory: ${(runtime.totalMemory() - runtime.freeMemory()) / mb}")
        logger.info("Free Memory: ${runtime.freeMemory() / mb}")
        logger.info("Total Memory:" + runtime.totalMemory() / mb)
        logger.info("Max Memory:" + runtime.maxMemory() / mb)
        logger.info("coroutineExecutor: ${(loritta.coroutineExecutor as ThreadPoolExecutor).activeCount}")
        // TODO - DeviousFun
        // logger.info("Global Rate Limit Hits in the last 10m: ${loritta.bucketedController?.getGlobalRateLimitHitsInTheLastMinute()} / ${loritta.config.loritta.discord.requestLimiter.maxRequestsPer10Minutes}")
        logger.info { "> Cache Stats" }
        for (shard in loritta.deviousShards.shards) {
            logger.info { "Shard ${shard.key} (${shard.value.deviousGateway.status.value}): Unavailable Guilds: ${shard.value.unavailableGuilds.size}; Guilds on this Shard: ${shard.value.guildsOnThisShard.size}; Pending Guilds Queues: ${shard.value.queuedGuildEvents.size}; Pending Events on the Queue: ${shard.value.deviousGateway.receivedEvents.size}" }
        }

        // TODO: Fix this
        /* logger.info { "Users: ${loritta.deviousShard.cacheManager.users.size} users" }
        logger.info { "Guilds: ${loritta.deviousShard.cacheManager.guilds.size} guilds" }
        logger.info { "Guild Channels: ${loritta.deviousShard.cacheManager.guildChannels.size} channels (${loritta.deviousShard.cacheManager.guildChannels.values.sumOf { it.size }} total)" }
        logger.info { "Channels to Guilds: ${loritta.deviousShard.cacheManager.channelsToGuilds.size}"}
        logger.info { "Guild Members: ${loritta.deviousShard.cacheManager.members.size} members (${loritta.deviousShard.cacheManager.members.values.sumOf { it.size }} total)" }
        logger.info { "Guild Roles: ${loritta.deviousShard.cacheManager.roles.size} guild roles (${loritta.deviousShard.cacheManager.roles.values.sumOf { it.size }} total)" }
        logger.info { "Guild Emojis: ${loritta.deviousShard.cacheManager.emotes.size} guild emojis (${loritta.deviousShard.cacheManager.emotes.values.sumOf { it.size }} total)" }
        logger.info { "Guild Voice States: ${loritta.deviousShard.cacheManager.voiceStates.size} guild voice states (${loritta.deviousShard.cacheManager.voiceStates.values.sumOf { it.size }} total)" } */
        logger.info("> Command Stuff")
        logger.info("commandManager.commandMap.size: ${loritta.legacyCommandManager.commandMap.size}")
        logger.info("messageInteractionCache.size: ${loritta.messageInteractionCache.size}")
        logger.info("locales.size: ${loritta.legacyLocales.size}")
        logger.info("ignoreIds.size: ${loritta.ignoreIds.size}")
        logger.info("> Tasks Stuff")
        logger.info("loritta.twitch.cachedGames: ${loritta.twitch.cachedGames.size}")
        logger.info("loritta.twitch.cachedStreamerInfo: ${loritta.twitch.cachedStreamerInfo.size}")
        logger.info("gameInfoCache.size: ${loritta.twitch.cachedGames.size}")
        logger.info("> Misc Stuff")
        logger.info("fanArts.size: ${loritta.fanArts.size}")
        logger.info("eventLogListener.downloadedAvatarJobs: ${EventLogListener.downloadedAvatarJobs}")
        // TODO - DeviousFun
        logger.info("> Executors")

        val pendingMessagesSize = loritta.pendingMessages.size
        val availableProcessors = LorittaBot.MESSAGE_EXECUTOR_THREADS
        val isMessagesOverloaded = pendingMessagesSize > availableProcessors
        logger.info(
            "Pending Messages ($pendingMessagesSize): Active: ${
                loritta.pendingMessages.filter { it.isActive }.count()
            }; Cancelled: ${
                loritta.pendingMessages.filter { it.isCancelled }.count()
            }; Complete: ${loritta.pendingMessages.filter { it.isCompleted }.count()};"
        )
        if (isMessagesOverloaded)
            logger.warn { "Loritta is overloaded! There are $pendingMessagesSize messages pending to be executed, ${pendingMessagesSize - availableProcessors} more than it should be!" }
    }
}