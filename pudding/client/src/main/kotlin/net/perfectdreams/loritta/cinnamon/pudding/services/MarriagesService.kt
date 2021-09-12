package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.UserId
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingMarriage
import net.perfectdreams.loritta.cinnamon.pudding.tables.Marriages
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select

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
}