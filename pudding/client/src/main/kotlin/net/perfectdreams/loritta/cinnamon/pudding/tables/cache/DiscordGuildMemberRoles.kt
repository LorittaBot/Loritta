package net.perfectdreams.loritta.cinnamon.pudding.tables.cache

import org.jetbrains.exposed.dao.id.LongIdTable

object DiscordGuildMemberRoles : LongIdTable() {
    val guildId = long("guild").index()
    val userId = long("user").index()
    val roleId = long("role").index()

    init {
        index(true, guildId, userId, roleId)
    }
}
