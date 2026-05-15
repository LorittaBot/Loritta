package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object CompletedCollections : LongIdTable() {
    val userId = long("user").index()
    val collection = reference("collection", Collections).index()
    val claimedAt = timestampWithTimeZone("claimed_at")

    init {
        uniqueIndex(userId, collection)
    }
}
