package net.perfectdreams.loritta.cinnamon.pudding.tables.notifications

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object DailyTaxWarnUserNotifications : LongIdTable() {
    val timestampLog = reference("timestamp_log", UserNotifications).index()
    val inactivityTaxTimeWillBeTriggeredAt = timestampWithTimeZone("inactivity_tax_time_will_be_triggered_at")
    val currentSonhos = long("current_sonhos")
    val howMuchWasRemoved = long("how_much_was_removed")
    val maxDayThreshold = integer("max_day_threshold")
    val minimumSonhosForTrigger = long("minimum_sonhos_for_trigger")
    val tax = double("tax")
}