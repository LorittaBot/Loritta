package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import net.perfectdreams.exposedpowerutils.sql.javatime.timestampWithTimeZone
import org.jetbrains.exposed.dao.id.LongIdTable

object GamerSaferGuildMembers : LongIdTable() {
    val guild = long("guild").index()
    val discordUser = long("discord_user").index()
    val gamerSaferUser = text("gamersafer_user").index()
}