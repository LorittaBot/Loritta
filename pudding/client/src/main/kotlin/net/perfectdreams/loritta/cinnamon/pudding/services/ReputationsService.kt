package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.Reputation
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.select

class ReputationsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun deleteReputation(reputationId: Long) = pudding.transaction {
        Reputations.deleteWhere {
            Reputations.id eq reputationId
        }
    }

    suspend fun getGivenReputationsByUser(userId: Long) = pudding.transaction {
        Reputations.select {
            Reputations.givenById eq userId
        }.map { Reputation.fromRow(it) }
    }

    suspend fun getReceivedReputationsByUser(userId: Long) = pudding.transaction {
        Reputations.select {
            Reputations.receivedById eq userId
        }.map { Reputation.fromRow(it) }
    }
}