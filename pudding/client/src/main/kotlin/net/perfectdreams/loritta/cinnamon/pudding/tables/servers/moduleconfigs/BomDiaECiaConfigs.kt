package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.loritta.cinnamon.pudding.tables.SnowflakeTable
import org.jetbrains.exposed.sql.javatime.timestampWithTimeZone

object BomDiaECiaConfigs : SnowflakeTable() {
    val enabled = bool("enabled").index()
    val blockedChannels = array<Long>("blocked_channels")
    val useBlockedChannelsAsAllowedChannels = bool("use_blocked_channels_as_allowed_channels")
    val updatedAt = timestampWithTimeZone("updated_at").nullable()
}