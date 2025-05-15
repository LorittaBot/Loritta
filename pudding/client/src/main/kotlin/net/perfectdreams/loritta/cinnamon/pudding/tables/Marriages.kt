package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object MarriagesOld : LongIdTable() {
    override val tableName
        get() = "marriages"

    val user1 = long("user1").index()
    val user2 = long("user2").index()
    val marriedSince = long("married_since")
}