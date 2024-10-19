package net.perfectdreams.loritta.morenitta.analytics

import net.dv8tion.jda.internal.JDAImpl
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService
import net.perfectdreams.loritta.cinnamon.pudding.tables.BoughtStocks
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.TickerPrices
import net.perfectdreams.loritta.cinnamon.pudding.tables.TotalSonhosStats
import net.perfectdreams.loritta.cinnamon.pudding.tables.stats.LorittaClusterStats
import net.perfectdreams.loritta.cinnamon.pudding.tables.stats.LorittaDiscordShardStats
import net.perfectdreams.loritta.morenitta.LorittaBot
import org.jetbrains.exposed.sql.*
import java.lang.management.ManagementFactory
import java.time.Instant

/**
 * A class that periodically stores stats to the database, "poor man's Prometheus"
 *
 * The reason I didn't want to use Prometheus is because I wanted to have more control on how I could store the data and stuffz, and because I already have more knowledge on how
 * PostgreSQL works compared to Prometheus, so I don't want to lose my stats because Prometheus decided to delete it or something like that.
 *
 * I also have more control of when the stats should be deleted.
 *
 * Yeah, one of the bad things is that I can't decide which stats gets deleted (because they are all in the same table)
 *
 * But well you ALSO CAN'T DO THAT IN PROMETHEUS (you can do that with VictoriaMetrics... Enterprise)
 */
class MagicStats(val loritta: LorittaBot) : RunnableCoroutine {
    override suspend fun run() {
        loritta.transaction {
            val now = Instant.now()

            val mb = 1024 * 1024
            val runtime = Runtime.getRuntime()
            val freeMemory = runtime.freeMemory() / mb
            val maxMemory = runtime.maxMemory() / mb
            val totalMemory = runtime.totalMemory() / mb

            val sumField = Profiles.money.sum()
            val totalSonhos = Profiles.select(sumField)
                .where {
                    Profiles.money greater 0
                }
                .first()[sumField] ?: 0

            val totalSonhosOfBannedUsers = Profiles.select(sumField)
                .where {
                    Profiles.money greater 0 and (Profiles.id inSubQuery UsersService.validBannedUsersList(System.currentTimeMillis()))
                }
                .first()[sumField] ?: 0

            val sonhosBrokerSumField = TickerPrices.value.sum()
            val totalSonhosBroker = BoughtStocks.innerJoin(TickerPrices, { BoughtStocks.ticker }, { TickerPrices.ticker })
                .select(sonhosBrokerSumField)
                .first()[sonhosBrokerSumField] ?: 0

            TotalSonhosStats.insert {
                it[TotalSonhosStats.timestamp] = Instant.now()
                it[TotalSonhosStats.totalSonhos] = totalSonhos
                it[TotalSonhosStats.totalSonhosOfBannedUsers] = totalSonhosOfBannedUsers
                it[TotalSonhosStats.totalSonhosBroker] = totalSonhosBroker
            }

            LorittaClusterStats.insert {
                it[LorittaClusterStats.timestamp] = now
                it[LorittaClusterStats.clusterId] = loritta.clusterId
                it[LorittaClusterStats.pendingMessagesCount] = loritta.pendingMessages.size
                it[LorittaClusterStats.freeMemory] = freeMemory
                it[LorittaClusterStats.maxMemory] = maxMemory
                it[LorittaClusterStats.totalMemory] = totalMemory
                it[LorittaClusterStats.threadCount] = ManagementFactory.getThreadMXBean().threadCount
                it[LorittaClusterStats.uptime] = ManagementFactory.getRuntimeMXBean().uptime
                it[LorittaClusterStats.puddingIdleConnections] = loritta.pudding.hikariDataSource.hikariPoolMXBean.idleConnections
                it[LorittaClusterStats.puddingActiveConnections] = loritta.pudding.hikariDataSource.hikariPoolMXBean.activeConnections
                it[LorittaClusterStats.puddingTotalConnections] = loritta.pudding.hikariDataSource.hikariPoolMXBean.totalConnections
            }

            val shardManager = loritta.lorittaShards.shardManager
            LorittaDiscordShardStats.batchInsert(shardManager.shardCache, shouldReturnGeneratedValues = false) {
                this[LorittaDiscordShardStats.timestamp] = now
                this[LorittaDiscordShardStats.shardId] = it.shardInfo.shardId
                this[LorittaDiscordShardStats.status] = it.status.name
                this[LorittaDiscordShardStats.gatewayStartupResumeStatus] = loritta.gatewayShardsStartupResumeStatus[it.shardInfo.shardId]?.name
                this[LorittaDiscordShardStats.gatewayPing] = it.gatewayPing
                this[LorittaDiscordShardStats.responseTotal] = it.responseTotal
                this[LorittaDiscordShardStats.guildsCount] = it.guildCache.size()
                this[LorittaDiscordShardStats.cachedUsersCount] = it.userCache.size()
                this[LorittaDiscordShardStats.cachedChannelsCount] = it.channelCache.size()
                this[LorittaDiscordShardStats.cachedRolesCount] = it.roleCache.size()
                this[LorittaDiscordShardStats.cachedEmojisCount] = it.emojiCache.size()
                this[LorittaDiscordShardStats.cachedAudioManagerCount] = it.audioManagerCache.size()
                this[LorittaDiscordShardStats.cachedScheduledEventsCount] = it.scheduledEventCache.size()

                // Stuff that requires casting
                this[LorittaDiscordShardStats.unavailableGuildsCount] = (it as JDAImpl).guildSetupController.unavailableGuilds.size().toLong()
            }
        }
    }
}