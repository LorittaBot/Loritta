package net.perfectdreams.loritta.cinnamon.pudding.tables.easter2023

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import net.perfectdreams.exposedpowerutils.sql.postgresEnumeration
import net.perfectdreams.loritta.common.utils.easter2023.EasterEggColor
import org.jetbrains.exposed.dao.id.LongIdTable

object Easter2023Drops : LongIdTable() {
    val guildId = long("guild")
    val channelId = long("channel")
    val messageId = long("message")
    val createdAt = timestampWithTimeZone("created_at")
    val eggColor = postgresEnumeration<EasterEggColor>("egg_color")
}