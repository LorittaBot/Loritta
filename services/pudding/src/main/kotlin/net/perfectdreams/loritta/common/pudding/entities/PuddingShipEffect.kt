package net.perfectdreams.loritta.common.pudding.entities

import net.perfectdreams.loritta.common.entities.ShipEffect

class PuddingShipEffect(val shipEffect: net.perfectdreams.loritta.pudding.common.data.ShipEffect) : ShipEffect {
    override val id by shipEffect::id
    override val buyerId by shipEffect::buyerId
    override val user1 by shipEffect::user1
    override val user2 by shipEffect::user2
    override val editedShipValue by shipEffect::editedShipValue
    override val expiresAt by shipEffect::expiresAt
}