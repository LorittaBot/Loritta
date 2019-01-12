package net.perfectdreams.loritta.utils.giveaway

import com.mrpowergamerbr.loritta.network.Databases
import mu.KotlinLogging
import net.perfectdreams.loritta.dao.Giveaway
import org.jetbrains.exposed.sql.transactions.transaction

class SpawnGiveawayTask : Runnable {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun run() {

        transaction(Databases.loritta) {
            val allActiveGiveaways = Giveaway.all()

            allActiveGiveaways.forEach {
                try {
                    if (GiveawayManager.giveawayTasks[it.id.value] == null)
                        GiveawayManager.createGiveawayJob(it)
                } catch (e: Exception) {
                    Companion.logger.error(e) { "Error while creating giveaway ${it.id.value} job" }
                }
            }
        }
    }
}