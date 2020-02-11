package net.perfectdreams.loritta.tables

import net.perfectdreams.loritta.utils.levels.LevelUpAnnouncementType
import org.jetbrains.exposed.dao.LongIdTable

object LevelAnnouncementConfigs : LongIdTable() {
    val levelConfig = reference("config", LevelConfigs)
    val type = enumeration("level_up_announcement_type", LevelUpAnnouncementType::class)
    val channelId = long("channel").nullable()
    val onlyIfUserReceivedRoles = bool("only_if_user_received_roles").default(false)
    val message = text("level_up_message")
}