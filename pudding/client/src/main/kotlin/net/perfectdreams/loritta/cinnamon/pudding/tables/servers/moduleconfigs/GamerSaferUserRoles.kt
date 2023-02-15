package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import org.jetbrains.exposed.dao.id.LongIdTable

object GamerSaferUserRoles : LongIdTable() {
    val guild = long("guild").index()
    val role = long("role").index()
    val user = long("user").index()
    val checkPeriod = long("check_period").index()
}