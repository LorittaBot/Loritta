package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import org.jetbrains.exposed.dao.id.LongIdTable

object GamerSaferRequiresVerificationRoles : LongIdTable() {
    val guild = long("guild").index()
    val role = long("role").index()
    // val checkPeriod = long("check_period").index()
}