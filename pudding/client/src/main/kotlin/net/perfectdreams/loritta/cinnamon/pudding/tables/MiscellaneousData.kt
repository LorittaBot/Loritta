package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.sql.Table

object MiscellaneousData : Table() {
    val id = text("id").index()
    val data = text("data")

    override val primaryKey = PrimaryKey(id)
}