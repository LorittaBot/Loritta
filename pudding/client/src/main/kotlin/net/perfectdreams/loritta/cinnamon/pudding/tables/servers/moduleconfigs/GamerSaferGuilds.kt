package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import org.jetbrains.exposed.dao.id.LongIdTable

object GamerSaferGuilds : LongIdTable() {
    val guildId = long("guild").index()
    val creationPayload = text("creation_payload")
}