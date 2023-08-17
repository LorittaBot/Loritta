package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.serializable.UserId
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingMarriage
import net.perfectdreams.loritta.cinnamon.pudding.tables.Marriages
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import org.jetbrains.exposed.sql.or

class MarriagesService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getMarriage(id: Long): PuddingMarriage? {
        return pudding.transaction {
            Marriages.selectFirstOrNull { Marriages.id eq id }
        }?.let { PuddingMarriage.fromRow(it) }
    }

    suspend fun getMarriageByUser(user: UserId): PuddingMarriage? {
        val userId = user.value.toLong()

        return pudding.transaction {
            Marriages.selectFirstOrNull { Marriages.user1 eq userId or (Marriages.user2 eq userId) }
        }?.let { PuddingMarriage.fromRow(it) }
    }
}