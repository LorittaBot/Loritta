package net.perfectdreams.loritta.utils.giveaway

import com.mrpowergamerbr.loritta.network.Databases
import com.mrpowergamerbr.loritta.utils.Constants
import mu.KotlinLogging
import net.perfectdreams.loritta.dao.servers.Giveaway
import net.perfectdreams.loritta.tables.servers.Giveaways
import org.jetbrains.exposed.sql.transactions.transaction

class SpawnGiveawayTask : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun run() {
        transaction(Databases.loritta) {
            val allActiveGiveaways = Giveaway.find { Giveaways.finished eq false }

            allActiveGiveaways.forEach {
                try {
                    if (it.finishAt >= System.currentTimeMillis() + Constants.DELAY_CUT_OFF) { // Não crie giveaways caso o tempo seja alto demais
                        logger.debug { "Not creating giveaway ${it.id.value}, it will expire at ${it.finishAt} and that's waaay too damn long!" }
                        return@forEach
                    }

                    if (GiveawayManager.giveawayTasks[it.id.value] == null)
                        GiveawayManager.createGiveawayJob(it)
                } catch (e: Exception) {
                    SpawnGiveawayTask.logger.error(e) { "Error while creating giveaway ${it.id.value} job" }
                }
            }
        }
    }
}