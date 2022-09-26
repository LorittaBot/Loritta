package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.common.achievements.AchievementType
import org.jetbrains.exposed.dao.id.LongIdTable

object UserAchievements : LongIdTable() {
    val user = reference("user", Profiles)
    val type = postgresEnumeration<AchievementType>("type")
    val achievedAt = timestampWithTimeZone("achieved_at")

    // Because "user" and "type" are primary keys, the database will reject things like
    // User1, FISHY_SHIP
    // User1, FISHY_SHIP (it will detect it is a duplicate!)
    //
    // However it will accept things like
    // User1, FISHY_SHIP
    // User2, FISHY_SHIP
    //
    // Nifty! https://github.com/JetBrains/Exposed/issues/239
    init {
        index(true, user, type)
    }
}