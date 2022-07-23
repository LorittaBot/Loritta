package net.perfectdreams.loritta.cinnamon.pudding.tables.cache

import net.perfectdreams.exposedpowerutils.sql.jsonb
import org.jetbrains.exposed.sql.Table

object DiscordVoiceStates : Table() {
    val guild = long("guild").index()
    val channel = long("channel").index()
    val user = long("user").index()
    val dataHashCode = integer("data_hash_code")
    val data = jsonb("data")

    init {
        index(true, guild, user)
    }
}
