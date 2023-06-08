package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable

object SonhosBundles : LongIdTable() {
    val active = bool("active").index()
    val sonhos = long("sonhos")
    val price = double("price")
    val bonus = long("bonus").nullable()
}