package net.perfectdreams.loritta.cinnamon.platform.utils.entitycache

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake

/**
 * Lazy and Cached permission checks
 */
class LazyCachedPermissions internal constructor(
    private val cacheService: DiscordCacheService,
    private val guildId: Snowflake,
    private val channelId: Snowflake,
    private val userId: Snowflake
) {
    private var permissions: Permissions? = null

    /**
     * Checks if the [userId] has permission to talk in the [channelId] on [guildId]
     */
    suspend fun canTalk() = hasPermission(Permission.SendMessages)

    /**
     * Checks if the [userId] has the following [permissions], or if they have [Permission.Administrator] if [adminPermissionsBypassesCheck] is enabled
     */
    suspend fun hasPermission(vararg permissions: Permission, adminPermissionsBypassesCheck: Boolean = true): Boolean {
        val localPermissions = this.permissions
        return if (localPermissions == null) {
            this.permissions = cacheService.getPermissions(guildId, channelId, userId)
            // Repeat the check! It shouldn't be null now
            hasPermission(*permissions)
        } else {
            if (adminPermissionsBypassesCheck && Permission.Administrator in localPermissions)
                return true

            permissions.all { it in localPermissions }
        }
    }
}