package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object GamerSaferSuccessfulVerifications : LongIdTable() {
    val guild = long("guild").index()
    val user = long("user").index()
    val role = long("role").index()
    val verifiedAt = timestampWithTimeZone("verified_at")
}