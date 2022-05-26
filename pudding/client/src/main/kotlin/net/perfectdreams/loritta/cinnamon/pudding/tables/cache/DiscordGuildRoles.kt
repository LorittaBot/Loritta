package net.perfectdreams.loritta.cinnamon.pudding.tables.cache

import org.jetbrains.exposed.dao.id.LongIdTable

object DiscordGuildRoles : LongIdTable() {
    val guildId = long("guild").index()
    val roleId = long("role").index()
    val name = text("name")
    val color = integer("color")
    val hoist = bool("hoist")
    val icon = text("icon").nullable()
    val unicodeEmoji = text("unicode_emoji").nullable()
    val position = integer("position")
    val permissions = long("permissions")
    val managed = bool("managed")
    val mentionable = bool("mentionable")

    init {
        index(true, guildId, roleId)
    }
}
