package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingMarriage
import net.perfectdreams.loritta.cinnamon.pudding.tables.Marriages
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.statements.InsertStatement

class MarriagesService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getMarriage(id: Long): PuddingMarriage? {
        return pudding.transaction {
            Marriages.select { Marriages.id eq id }
                .firstOrNull()
        }?.let { PuddingMarriage.fromRow(it) }
    }

    suspend fun getMarriageByUser(user: UserId): PuddingMarriage? {
        val userId = user.value.toLong()

        return pudding.transaction {
            Marriages.select { Marriages.user1 eq userId or (Marriages.user2 eq userId) }
                .firstOrNull()
        }?.let { PuddingMarriage.fromRow(it) }
    }

    suspend fun deleteMarriage(id: Long) =
        pudding.transaction {
            Marriages.deleteWhere { Marriages.id eq id }
        }

    suspend fun createMarriage(user1: UserId, user2: UserId): InsertStatement<Number> {
        val user1Id = user1.value.toLong()
        val user2Id = user2.value.toLong()

        return pudding.transaction {
            val insertStatement = Marriages.insert {
                it[Marriages.user1] = user1Id
                it[Marriages.user2] = user2Id
                it[Marriages.marriedSince] = System.currentTimeMillis()
            }

            insertStatement
        }
    }
}