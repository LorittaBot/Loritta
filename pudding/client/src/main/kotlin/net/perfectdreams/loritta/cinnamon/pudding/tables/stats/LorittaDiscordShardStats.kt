package net.perfectdreams.loritta.cinnamon.pudding.tables.stats

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object LorittaDiscordShardStats : LongIdTable() {
    val timestamp = timestampWithTimeZone("timestamp").index()
    val shardId = integer("shard_id").index()
    val status = text("status")
    val gatewayStartupResumeStatus = text("gateway_startup_resume_status").nullable()
    val gatewayPing = long("gateway_ping")
    val responseTotal = long("response_total")
    val guildsCount = long("guild_count")
    val unavailableGuildsCount = long("unavailable_guilds_count")
    val cachedUsersCount = long("cached_users_count")
    val cachedChannelsCount = long("cached_channels_count")
    val cachedRolesCount = long("cached_roles_count")
    val cachedEmojisCount = long("cached_emojis_count")
    val cachedAudioManagerCount = long("cached_audiomanager_count")
    val cachedScheduledEventsCount = long("cached_scheduled_events_count")
}