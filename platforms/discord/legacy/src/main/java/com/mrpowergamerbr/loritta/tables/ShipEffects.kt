package com.mrpowergamerbr.loritta.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object ShipEffects : LongIdTable() {
    val buyerId = long("buyer").index()
    val user1Id = long("user_1").index()
    val user2Id = long("user_2").index()
    val editedShipValue = integer("edited_ship_value") // Valor de 0 a 100
    val expiresAt = long("expires_at")
}