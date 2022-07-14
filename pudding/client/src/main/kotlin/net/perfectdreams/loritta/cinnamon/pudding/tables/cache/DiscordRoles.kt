package net.perfectdreams.loritta.cinnamon.pudding.tables.cache

import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.sql.Table

object DiscordRoles : Table() {
    val guild = long("guild").index()
    val role = long("role")
    val dataHashCode = integer("data_hash_integer")
    val data = jsonb("data")

    init {
        index(true, guild, role)
    }
}
