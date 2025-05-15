package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.sql.ReferenceOption


object Profiles : SnowflakeTable() {
    val xp = long("xp").index()
    val lastMessageSentAt = long("last_message_sent_at")
    val lastMessageSentHash = integer("last_message_sent_hash")
    val lastCommandSentAt = long("last_command_sent_at").nullable()
    val money = long("money").index()
    var isAfk = bool("isAfk")
    var afkReason = text("afkReason").nullable()
    var settings = reference("settings", UserSettings, onDelete = ReferenceOption.CASCADE).index()
    // DON'T SET THIS TO ON DELETE CASCADE!
    // If this is set to on delete cascade, if someone married deletes their user data, the user data of the user
    // AND the data of the user that is married with will be deleted!
    // var marriage = reference("marriage", Marriages).nullable().index()
    val vacationUntil = timestampWithTimeZone("vacation_until").nullable()
}