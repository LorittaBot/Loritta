package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.jsonb
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object ExecutedApplicationCommandsLog : LongIdTable() {
    val userId = long("user")
    val guildId = long("guild").nullable()
    val channelId = long("channel")
    val sentAt = timestamp("sent_at")

    val declaration = text("declaration")
    val executor = text("executor")
    val options = jsonb("options")
    val success = bool("success")
    val latency = double("latency")
    val stacktrace = text("stacktrace").nullable()

    // Sent At must be a primary key because it is used as a partition key
    // While this means that all partitions should have an unique ID and Sent At, the ID is always incrementing so I don't think
    // that this will cause issues
    override val primaryKey = PrimaryKey(id, sentAt)
}