package net.perfectdreams.loritta.cinnamon.discord.utils.entitycache

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.rest.service.RestClient
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.CinnamonMetrics

/**
 * Lazy and Cached permission checks
 */
class LazyCachedPermissions internal constructor(private val permissions: Permissions) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Checks if the [userId] has permission to talk in the [channelId] on [guildId]
     */
    suspend fun canTalk() = hasPermission(Permission.SendMessages)

    /**
     * Checks if the [userId] has the following [permissions], or if they have [Permission.Administrator] if [adminPermissionsBypassesCheck] is enabled
     */
    fun hasPermission(vararg permissions: Permission, adminPermissionsBypassesCheck: Boolean = true): Boolean {
        if (adminPermissionsBypassesCheck && Permission.Administrator in permissions)
            return true

        return permissions.all { it in permissions }
    }
}