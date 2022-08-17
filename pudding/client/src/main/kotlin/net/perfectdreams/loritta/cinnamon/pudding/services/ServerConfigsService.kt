package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.utils.LorittaPermission
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.data.InviteBlockerConfig
import net.perfectdreams.loritta.cinnamon.pudding.data.MiscellaneousConfig
import net.perfectdreams.loritta.cinnamon.pudding.data.ModerationConfig
import net.perfectdreams.loritta.cinnamon.pudding.data.StarboardConfig
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingServerConfigRoot
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.ServerRolePermissions
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.InviteBlockerConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.MiscellaneousConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.ModerationConfigs
import net.perfectdreams.loritta.cinnamon.pudding.tables.servers.moduleconfigs.StarboardConfigs
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirst
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.util.*

class ServerConfigsService(private val pudding: Pudding) : Service(pudding) {
    suspend fun getServerConfigRoot(guildId: ULong): PuddingServerConfigRoot? = pudding.transaction {
        ServerConfigs.selectFirstOrNull {
            ServerConfigs.id eq guildId.toLong()
        }?.let { PuddingServerConfigRoot.fromRow(it) }
    }

    suspend fun getModerationConfigByGuildId(guildId: ULong): ModerationConfig? = pudding.transaction {
        ModerationConfigs.innerJoin(ServerConfigs).selectFirstOrNull {
            ServerConfigs.id eq guildId.toLong()
        }?.let { ModerationConfig.fromRow(it) }
    }

    suspend fun getStarboardConfigById(id: Long): StarboardConfig? = pudding.transaction {
        StarboardConfigs.selectFirstOrNull {
            StarboardConfigs.id eq id
        }?.let { StarboardConfig.fromRow(it) }
    }

    suspend fun getMiscellaneousConfigById(id: Long): MiscellaneousConfig? {
        return pudding.transaction {
            MiscellaneousConfigs.selectFirstOrNull {
                MiscellaneousConfigs.id eq id
            }
        }?.let { MiscellaneousConfig.fromRow(it) }
    }

    suspend fun getInviteBlockerConfigById(id: Long): InviteBlockerConfig? {
        return pudding.transaction {
            InviteBlockerConfigs.selectFirstOrNull {
                InviteBlockerConfigs.id eq id
            }
        }?.let { InviteBlockerConfig.fromRow(it) }
    }

    /**
     * Gets the [LorittaPermission] that the [roleIds] on [guildId] has.
     */
    suspend fun getLorittaPermissionsOfRoles(guildId: ULong, roleIds: List<ULong>): Map<Long, EnumSet<LorittaPermission>> {
        return pudding.transaction {
            // Pull the permissions from the database
            val permissions = ServerRolePermissions.select {
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