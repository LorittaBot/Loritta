package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingShipEffect
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.serializable.UserId
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.selectAll

class ShipEffectsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getShipEffectsForUser(user: UserId): List<PuddingShipEffect> {
        val userId = user.value.toLong()

        val effects = pudding.transaction {
            ShipEffects.selectAll().where {
                (ShipEffects.user1Id eq userId) or (ShipEffects.user2Id eq userId)
            }.toList()
        }

        return effects.map { PuddingShipEffect.fromRow(it) }
    }
}