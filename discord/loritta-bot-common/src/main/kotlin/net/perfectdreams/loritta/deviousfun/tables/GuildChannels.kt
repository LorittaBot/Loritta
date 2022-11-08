package net.perfectdreams.loritta.deviousfun.tables

import net.perfectdreams.loritta.deviousfun.tables.GuildEmojis.index
import org.jetbrains.exposed.sql.Table

object GuildChannels : Table() {
    val id = long("id").index()
    val data = text("data")

    override val primaryKey = PrimaryKey(id)
}