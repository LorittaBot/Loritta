package net.perfectdreams.loritta.morenitta.tables.servers.moduleconfigs

import net.perfectdreams.loritta.morenitta.utils.exposed.array
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.LongColumnType

object RolesByExperience : LongIdTable() {
    val guildId = long("guild").index()
    val requiredExperience = long("required_experience").index()
    val roles = array<Long>("role_id", LongColumnType())
}