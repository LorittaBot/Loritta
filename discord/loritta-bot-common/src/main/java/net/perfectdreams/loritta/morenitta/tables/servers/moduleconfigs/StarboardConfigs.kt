package net.perfectdreams.loritta.morenitta.tables.servers.moduleconfigs

import org.jetbrains.exposed.dao.id.LongIdTable

object StarboardConfigs : LongIdTable() {
    val enabled = bool("enabled").default(false)
    val starboardChannelId = long("starboard_channel")
    val requiredStars = integer("required_stars")
}