package net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object CreatedEaster2023Baskets : LongIdTable() {
    val user = reference("user", Easter2023Players).index()
    val createdAt = timestampWithTimeZone("created_at")
}