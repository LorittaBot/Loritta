package net.perfectdreams.loritta.cinnamon.pudding.entities

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.ShipEffect

data class PuddingShipEffect(
    private val pudding: Pudding,
    val data: ShipEffect
) {
    companion object;

    val id by data::id
    val buyerId by data::buyerId
    val user1 by data::user1
    val user2 by data::user2
    val editedShipValue by data::editedShipValue
    val expiresAt by data::expiresAt
}