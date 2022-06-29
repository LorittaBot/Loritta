package net.perfectdreams.loritta.cinnamon.pudding.tables.notifications

import net.perfectdreams.loritta.cinnamon.pudding.tables.TrackedCorreiosPackagesEvents
import org.jetbrains.exposed.dao.id.LongIdTable

object CorreiosPackageUpdateUserNotifications : LongIdTable() {
    val timestampLog = reference("timestamp_log", UserNotifications).index()
    val trackingId = text("tracking_id")
    val packageEvent = reference("package_event", TrackedCorreiosPackagesEvents)
}