package net.perfectdreams.loritta.cinnamon.pudding.tables.cache

import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.sql.Table

object DiscordGuildMembers : Table() {
    val guildId = long("guild").index()
    val userId = long("user").index()
    val roles = jsonb("roles")

    init {
        index(true, guildId, userId)
    }
}
