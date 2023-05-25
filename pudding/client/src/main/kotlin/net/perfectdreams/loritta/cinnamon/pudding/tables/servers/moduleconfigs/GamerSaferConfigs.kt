package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import org.jetbrains.exposed.dao.id.LongIdTable

object GamerSaferConfigs : LongIdTable() {
    val verifiedRoleId = long("verified_role_id").nullable()
}