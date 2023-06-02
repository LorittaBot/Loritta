package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.Reputation
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere

class PuddingReputation(
    private val pudding: Pudding,
    val data: Reputation
) {
    companion object;

    val id by data::id
    val givenById by data::givenById
    val givenByIp by data::givenByIp
    val givenByEmail by data::givenByEmail
    val receivedById by data::receivedById
    val receivedAt by data::receivedAt
    val content by data::content

    suspend fun delete() = pudding.transaction {
        Reputations.deleteWhere {
            Reputations.id eq this@PuddingReputation.id
        }
    }
}