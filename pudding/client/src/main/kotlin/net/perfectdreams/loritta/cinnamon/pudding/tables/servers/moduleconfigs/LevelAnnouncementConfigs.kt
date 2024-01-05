package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.loritta.serializable.levels.LevelUpAnnouncementType
import org.jetbrains.exposed.dao.id.LongIdTable

object LevelAnnouncementConfigs : LongIdTable() {
    val levelConfig = reference("config", LevelConfigs).index()
    val type = enumeration("level_up_announcement_type", LevelUpAnnouncementType::class)
    val channelId = long("channel").nullable()
    val onlyIfUserReceivedRoles = bool("only_if_user_received_roles").default(false)
    val message = text("level_up_message")
}