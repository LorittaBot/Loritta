package net.perfectdreams.loritta.lorituber.server.tables

import org.jetbrains.exposed.sql.Table

object LoriTuberGroceryStores : Table() {
    val shop = text("shop").uniqueIndex()
    val data = blob("data")
}