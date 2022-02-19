package net.perfectdreams.loritta.cinnamon.pudding.tables

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object TaskQueue : LongIdTable() {
    val queueTime = timestamp("queue_time")
    val payload = text("payload")
}