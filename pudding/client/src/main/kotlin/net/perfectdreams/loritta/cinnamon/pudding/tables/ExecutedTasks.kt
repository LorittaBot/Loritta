package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IdTable
import org.jetbrains.exposed.sql.Column

object GlobalTasks : IdTable<String>() {
    override val id: Column<EntityID<String>> = text("id").uniqueIndex().entityId()
    val lastExecutedByClusterId = integer("last_executed_by_cluster_id")
    val lastExecutedAt = timestampWithTimeZone("last_executed_at")
}