package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object BackgroundPayments : LongIdTable() {
    val userId = long("user").index()
    val background = reference("background", Backgrounds).index()
    val boughtAt = long("bought_at")
    val cost = long("money")
}