package net.perfectdreams.loritta.cinnamon.pudding.tables.stats

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object LorittaClusterStats : LongIdTable() {
    val timestamp = timestampWithTimeZone("timestamp").index()
    val clusterId = integer("cluster_id").index()
    val pendingMessagesCount = integer("pending_messages_count")
    val freeMemory = long("free_memory")
    val maxMemory = long("max_memory")
    val totalMemory = long("total_memory")
    val threadCount = integer("thread_count")
    val uptime = long("uptime")
    val puddingIdleConnections = integer("pudding_idle_connections")
    val puddingActiveConnections = integer("pudding_active_connections")
    val puddingTotalConnections = integer("pudding_total_connections")
}