package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingReputation
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import org.jetbrains.exposed.sql.select

class ReputationsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getGivenReputationsByUser(userId: Long) = pudding.transaction {
        Reputations.select {
            Reputations.givenById eq userId
        }.map { PuddingReputation.fromRow(it) }
    }

    suspend fun getReceivedReputationsByUser(userId: Long) = pudding.transaction {
        Reputations.select {
            Reputations.receivedById eq userId
        }.map { PuddingReputation.fromRow(it) }
    }

    suspend fun getReputation(reputationId: Long) = pudding.transaction {
        Reputations.selectFirstOrNull {
            Reputations.id eq reputationId
        }?.let { PuddingReputation.fromRow(it) }
    }
}