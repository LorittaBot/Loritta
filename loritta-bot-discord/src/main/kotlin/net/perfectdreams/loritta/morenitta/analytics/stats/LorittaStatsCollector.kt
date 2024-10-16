package net.perfectdreams.loritta.morenitta.analytics.stats

import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.RunnableCoroutine
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.ClusterNotReadyException
import net.perfectdreams.loritta.morenitta.utils.ClusterOfflineException
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.sum

class LorittaStatsCollector(val m: LorittaBot) : RunnableCoroutine {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private val senders = listOf(
        TopggStatsSender(m.http, m.config.loritta.voteWebsites.topgg.clientId, m.config.loritta.voteWebsites.topgg.token),
        DiscordBotsStatsSender(m.http, m.config.loritta.voteWebsites.discordBots.clientId, m.config.loritta.voteWebsites.discordBots.token),
        DatabaseStatsSender(m.pudding)
    )

    override suspend fun run() {
        try {
            logger.info { "Collecting stats data from Loritta..." }
            val guildCount = try {
                m.lorittaShards.queryGuildCountOrThrowExceptionIfAnyClusterIsNotReady()
            } catch (e: ClusterOfflineException) {
                logger.warn(e) { "Cluster ${e.name} is offline! Skipping stats collection task..." }
                return
            }  catch (e: ClusterNotReadyException) {
                logger.warn(e) { "Cluster ${e.name} is not ready! Skipping stats collection task..." }
                return
            }

            val (totalSonhos, totalSonhosOfBannedUsers) = m.transaction {
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

                return@transaction Pair(totalSonhos, totalSonhosOfBannedUsers)
            }

            senders.forEach {
                try {
                    it.send(guildCount.toLong(), totalSonhos, totalSonhosOfBannedUsers)
                    logger.info { "Successfully sent Loritta's stats data to ${it}!" }
                } catch (e: Exception) {
                    logger.warn(e) { "Something went wrong while trying to send Loritta's stats data to ${it}!" }
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Something went wrong while collecting and sending stats data!" }
        }
    }
}