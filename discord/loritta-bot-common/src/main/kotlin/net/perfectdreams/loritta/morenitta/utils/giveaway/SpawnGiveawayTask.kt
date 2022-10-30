package net.perfectdreams.loritta.morenitta.utils.giveaway

import kotlinx.coroutines.runBlocking
import net.perfectdreams.loritta.morenitta.utils.Constants
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.servers.Giveaway
import net.perfectdreams.loritta.morenitta.tables.servers.Giveaways
import net.perfectdreams.loritta.morenitta.utils.DiscordUtils

class SpawnGiveawayTask(val loritta: LorittaBot) : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun run() {
        runBlocking {
            loritta.pudding.transaction {
                val allActiveGiveaways = Giveaway.find { Giveaways.finished eq false }

                allActiveGiveaways.forEach {
                    try {
                        if (it.finishAt >= System.currentTimeMillis() + Constants.DELAY_CUT_OFF) { // Não crie giveaways caso o tempo seja alto demais
                            logger.debug { "Not creating giveaway ${it.id.value}, it will expire at ${it.finishAt} and that's waaay too damn long!" }
                            return@forEach
                        }

                        if (loritta.giveawayManager.giveawayTasks[it.id.value] == null && DiscordUtils.isCurrentClusterHandlingGuildId(
                                loritta,
                                it.guildId
                            )
                        )
                            loritta.giveawayManager.createGiveawayJob(it)
                    } catch (e: Exception) {
                        logger.error(e) { "Error while creating giveaway ${it.id.value} job" }
                    }
                }
            }
        }
    }
}