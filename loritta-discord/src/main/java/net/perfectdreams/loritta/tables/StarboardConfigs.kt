package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object StarboardConfigs : LongIdTable() {
    val enabled = bool("enabled").default(false)
    val starboardChannelId = long("starboard_channel")
    val requiredStars = integer("required_stars")
}