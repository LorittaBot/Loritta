package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object PremiumTrackTwitchAccounts : LongIdTable() {
    val twitchUserId = long("twitch_user_id").uniqueIndex()
    val guildId = long("guild_id").index()
    val addedBy = long("added_by")
    val addedAt = timestampWithTimeZone("added_at")

    init {
        uniqueIndex(twitchUserId, guildId)
    }
}