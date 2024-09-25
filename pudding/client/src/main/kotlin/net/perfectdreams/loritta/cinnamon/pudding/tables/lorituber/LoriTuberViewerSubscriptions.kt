package net.perfectdreams.loritta.cinnamon.pudding.tables.lorituber

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object LoriTuberViewerSubscriptions : LongIdTable() {
    val owner = reference("owner", LoriTuberViewers).index()
    val channel = reference("channel", LoriTuberChannels).index()
    // TODO: Implement this!
    // val videoThatCausedTheSubscription =
    val subscribedAt = timestampWithTimeZone("subscribed_at")
}