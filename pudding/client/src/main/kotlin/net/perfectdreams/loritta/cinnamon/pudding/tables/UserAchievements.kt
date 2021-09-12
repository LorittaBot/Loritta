package net.perfectdreams.loritta.cinnamon.pudding.tables

import net.perfectdreams.loritta.cinnamon.common.achievements.AchievementType
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.postgresEnumeration
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.`java-time`.timestamp

object UserAchievements : LongIdTable() {
    val user = reference("user", Profiles)
    val type = postgresEnumeration<AchievementType>("type")
    val achievedAt = timestamp("achieved_at")

    // Because "user" and "type" are primary keys, the database will reject things like
    // User1, FISHY_SHIP
    // User1, FISHY_SHIP (it will detect it is a duplicate!)
    //
    // However it will accept things like
    // User1, FISHY_SHIP
    // User2, FISHY_SHIP
    //
    // Nifty! https://github.com/JetBrains/Exposed/issues/239
    override val primaryKey = PrimaryKey(user, type)
}