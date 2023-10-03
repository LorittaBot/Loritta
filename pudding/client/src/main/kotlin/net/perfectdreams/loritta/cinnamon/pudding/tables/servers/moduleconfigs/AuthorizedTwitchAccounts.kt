package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object AuthorizedTwitchAccounts : LongIdTable() {
    val userId = long("twitch_user_id").uniqueIndex()
    val authorizedAt = timestampWithTimeZone("authorized_at")
}