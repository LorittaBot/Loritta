package net.perfectdreams.loritta.tables

import org.jetbrains.exposed.dao.LongIdTable

object ExperienceRoleRates : LongIdTable() {
    val guildId = long("guild").index()
    val role = long("role").index()
    val rate = double("rate")
}