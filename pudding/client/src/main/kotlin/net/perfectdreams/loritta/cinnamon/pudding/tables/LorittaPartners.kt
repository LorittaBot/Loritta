package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object LorittaPartners : LongIdTable() {
    val guildId = long("guild_id").index()
    val acceptedBy = long("accepted_by").index()
    val acceptedAt = timestampWithTimeZone("accepted_at").index()
}
