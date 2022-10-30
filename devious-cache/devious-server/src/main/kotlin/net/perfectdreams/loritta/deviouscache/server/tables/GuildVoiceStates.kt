package net.perfectdreams.loritta.deviouscache.server.tables

import org.jetbrains.exposed.sql.Table

object GuildVoiceStates : Table() {
    val id = long("id").index()
    val data = text("data")

    override val primaryKey = PrimaryKey(id)
}