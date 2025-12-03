package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.*
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.common.utils.PunishmentAction
import net.perfectdreams.loritta.serializable.*
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class ServerConfigsService(private val pudding: Pudding) : Service(pudding) {
    /**
     * Gets the [LorittaPermission] that the [roleIds] on [guildId] has.
     */
    suspend fun getLorittaPermissionsOfRoles(guildId: ULong, roleIds: List<ULong>): Map<Long, EnumSet<LorittaPermission>> {
        if (roleIds.isEmpty())
            return emptyMap()

        return pudding.transaction {
            // Pull the permissions from the database
            val permissions = ServerRolePermissions.selectAll().where {
                ServerRolePermissions.guild eq guildId.toLong() and (ServerRolePermissions.roleId inList roleIds.map { it.toLong() })
            }

            // Create a enum set
            val enumSet = permissions
                .asSequence()
                .map { it[ServerRolePermissions.roleId] to it[ServerRolePermissions.permission] }
                .groupBy { it.first }
                .map { it.key to it.value.map { it.second } }
                .associate { it.first to EnumSet.copyOf(it.second) }

            return@transaction enumSet
        }
    }

    /**
     * Checks if the [roleIds] on [guildId] has all the [permission].
     */
    suspend fun hasLorittaPermission(guildId: ULong, roleIds: List<ULong>, vararg permission: LorittaPermission): Boolean {
        val permissions = getLorittaPermissionsOfRoles(guildId, roleIds)
        return permissions.values.any { permission.all { perm -> it.contains(perm) } }
    }
}