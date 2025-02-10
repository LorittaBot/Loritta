package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object ReceivedUpdatedGuidelinesNotifications : LongIdTable() {
    val user = reference("user", Profiles).index()
    val receivedAt = timestampWithTimeZone("received_at")
    val rulesId = integer("rules_id").index()

    init {
        uniqueIndex(user, rulesId)
    }
}