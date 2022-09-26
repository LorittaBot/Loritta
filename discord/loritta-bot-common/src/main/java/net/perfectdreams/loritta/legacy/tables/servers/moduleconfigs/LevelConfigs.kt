package net.perfectdreams.loritta.legacy.tables.servers.moduleconfigs

import net.perfectdreams.loritta.legacy.utils.exposed.array
import net.perfectdreams.loritta.legacy.utils.levels.RoleGiveType
import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.LongColumnType

object LevelConfigs : LongIdTable() {
    val roleGiveType = enumeration("role_give_type", RoleGiveType::class)
    val noXpRoles = array<Long>("no_xp_roles", LongColumnType())
    val noXpChannels = array<Long>("no_xp_channels", LongColumnType())
}