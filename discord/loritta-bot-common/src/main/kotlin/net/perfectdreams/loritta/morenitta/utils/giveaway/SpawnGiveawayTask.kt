package net.perfectdreams.loritta.morenitta.utils.giveaway

import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.morenitta.utils.Constants
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.servers.Giveaway
import net.perfectdreams.loritta.morenitta.tables.servers.Giveaways
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils
import org.jetbrains.exposed.sql.and

class SpawnGiveawayTask(val loritta: LorittaBot) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun run() {
        runBlocking {
            loritta.pudding.transaction {
                val timeCutOff = System.currentTimeMillis() + Constants.DELAY_CUT_OFF

                val allActiveGiveaways = Giveaway.find {
                    Giveaways.finished eq false and (Giveaways.finishAt lessEq timeCutOff)
                }

                allActiveGiveaways.forEach {
                    try {
                        if (DiscordUtils.getLorittaClusterForGuildId(loritta, it.guildId).id == loritta.clusterId) {
                            val shardId = DiscordUtils.getShardIdFromGuildId(loritta, it.guildId)
                            val shard = loritta.lorittaShards.shardManager.getShardById(shardId)
                            if (shard != null) {
                                shard.awaitReady()

                                if (loritta.giveawayManager.giveawayTasks[it.id.value] == null)
                                    loritta.giveawayManager.createGiveawayJob(it)
                            }
                        }
                    } catch (e: Exception) {
                        logger.error(e) { "Error while creating giveaway ${it.id.value} job" }
                    }
                }
            }
        }
    }
}