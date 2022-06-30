package net.perfectdreams.loritta.cinnamon.pudding.tables.cache

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.Table

object DiscordGuildMemberRoles : Table() {
    val guildId = long("guild").index()
    val userId = long("user").index()
    val roleId = long("role").index()

    init {
        index(true, guildId, userId, roleId)
    }
}
