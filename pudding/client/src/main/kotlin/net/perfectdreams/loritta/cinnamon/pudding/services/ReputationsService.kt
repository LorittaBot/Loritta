package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingReputation
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import org.jetbrains.exposed.sql.select

class ReputationsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getLastReputationGiven(userId: Long) =
        pudding.transaction {
            Reputations.select {
                Reputations.givenById eq userId
            }.orderBy(Reputations.receivedAt).limit(1).firstOrNull()?.let {
                PuddingReputation.fromRow(it)
            }
        }
}