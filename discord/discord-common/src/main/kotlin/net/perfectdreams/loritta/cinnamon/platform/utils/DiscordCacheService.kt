package net.perfectdreams.loritta.cinnamon.platform.utils

import dev.kord.common.entity.DiscordRole
import dev.kord.common.entity.Snowflake
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.cache.DiscordGuilds
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.selectFirstOrNull

class DiscordCacheService(private val pudding: Pudding) {
    suspend fun getRoles(guildId: Snowflake, roleIds: List<Snowflake>): List<DiscordRole> {
        return pudding.transaction {
            val roles = DiscordGuilds.slice(DiscordGuilds.roles)
                .selectFirstOrNull {
                    DiscordGuilds.id eq guildId.toLong()
                }?.get(DiscordGuilds.roles) ?: return@transaction emptyList()

            /* return@transaction Json.decodeFromString<PuddingDiscordRolesMap>(roles)
                .values
                .filter { it.id in roleIds } */
            TODO()
        }
    }
}