package net.perfectdreams.loritta.cinnamon.platform.utils

import dev.kord.common.entity.DiscordRole
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.optional
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuildRoles
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class DiscordCacheService(private val pudding: Pudding) {
    suspend fun getRoles(guildId: Snowflake, roleIds: List<Snowflake>): List<DiscordRole> {
        return pudding.transaction {
            DiscordGuildRoles.select {
                (DiscordGuildRoles.guildId eq guildId.toLong()) and (DiscordGuildRoles.roleId inList roleIds.map { it.toLong() })
            }.map {
                DiscordRole(
                    Snowflake(it[DiscordGuildRoles.roleId]),
                    it[DiscordGuildRoles.name],
                    it[DiscordGuildRoles.color],
                    it[DiscordGuildRoles.hoist],
                    it[DiscordGuildRoles.icon].optional(),
                    it[DiscordGuildRoles.unicodeEmoji].optional(),
                    it[DiscordGuildRoles.position],
                    Permissions(it[DiscordGuildRoles.permissions].toString()),
                    it[DiscordGuildRoles.managed],
                    it[DiscordGuildRoles.mentionable],
                    Optional()
                )
            }
        }
    }
}