package net.perfectdreams.loritta.cinnamon.pudding.services

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.entities.PuddingServerConfigRoot
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

    suspend fun getMessageForPunishmentTypeOnGuildId(guildId: ULong, punishmentAction: PunishmentAction): String? = pudding.transaction {
        val moderationConfig = getModerationConfigByGuildId(guildId)

        val moderationPunishmentMessageConfig = ModerationPunishmentMessagesConfig.selectFirstOrNull {
            ModerationPunishmentMessagesConfig.guild eq guildId.toLong() and
                    (ModerationPunishmentMessagesConfig.punishmentAction eq punishmentAction)
        }

        moderationPunishmentMessageConfig?.get(ModerationPunishmentMessagesConfig.punishLogMessage) ?: moderationConfig?.punishLogMessage
    }

    suspend fun getPredefinedPunishmentMessagesByGuildId(guildId: ULong) = pudding.transaction {
        ModerationPredefinedPunishmentMessages.selectAll().where {
            ModerationPredefinedPunishmentMessages.guild eq guildId.toLong()
        }.map {
            PredefinedPunishmentMessage(it[ModerationPredefinedPunishmentMessages.short], it[ModerationPredefinedPunishmentMessages.message])
        }
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