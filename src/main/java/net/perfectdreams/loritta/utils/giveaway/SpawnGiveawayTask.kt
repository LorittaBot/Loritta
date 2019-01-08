package net.perfectdreams.loritta.utils.giveaway

import com.mrpowergamerbr.loritta.network.Databases
import net.perfectdreams.loritta.dao.Giveaway
import net.perfectdreams.loritta.tables.Giveaways
import org.jetbrains.exposed.sql.transactions.transaction

class SpawnGiveawayTask : Runnable {
    override fun run() {
        transaction(Databases.loritta) {
            val allActiveGiveaways = Giveaway.all()

            allActiveGiveaways.forEach {
                GiveawayManager.createGiveawayJob(it)
            }
        }
    }
}