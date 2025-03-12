package net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs

import org.jetbrains.exposed.dao.id.LongIdTable
import org.jetbrains.exposed.sql.LongColumnType

object AutoroleConfigs : LongIdTable() {
    val enabled = bool("enabled").default(false)
    val giveOnlyAfterMessageWasSent = bool("give_only_after_message_was_sent").default(true)
    val roles = array<Long>("roles", LongColumnType())
    val giveRolesAfter = long("give_roles_after").nullable()
}