package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import org.jetbrains.exposed.dao.id.LongIdTable

object DonationConfigs : LongIdTable() {
    val customBadge = bool("custom_badge").default(false)
    val customBadgeFile = text("custom_badge_file").nullable()
    val customBadgePreferredMediaType = text("custom_badge_preferred_media_type").nullable()
    val dailyMultiplier = bool("daily_multiplier").default(false)
}