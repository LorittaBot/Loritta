package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object Marriages : LongIdTable() {
    val user = long("user").index()
    val partner = long("partner").index()
    val marriedSince = long("married_since")
}