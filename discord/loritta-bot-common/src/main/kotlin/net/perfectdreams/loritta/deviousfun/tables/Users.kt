package net.perfectdreams.loritta.deviousfun.tables

import org.jetbrains.exposed.sql.Table

object Users : Table() {
    val id = long("id").index()
    val data = text("data")

    override val primaryKey = PrimaryKey(id)
}