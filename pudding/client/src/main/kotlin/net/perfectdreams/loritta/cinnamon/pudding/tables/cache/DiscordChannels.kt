package net.perfectdreams.loritta.cinnamon.pudding.tables.cache

import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.sql.Table

object DiscordChannels : Table() {
    val guild = long("guild").index()
    val channel = long("channel")
    val dataHashCode = integer("data_hash_code")
    val data = jsonb("data")

    init {
        index(true, guild, channel)
    }
}
