package net.perfectdreams.loritta.cinnamon.discord.utils.entitycache

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.rest.service.RestClient
import mu.KotlinLogging
import net.perfectdreams.loritta.morenitta.LorittaBot

/**
 * Lazy and Cached permission checks
 */
abstract class LazyCachedPermissions internal constructor(
    private val rest: RestClient,
    private val loritta: LorittaBot,
    private val cacheService: DiscordCacheService
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    private var permissions: Permissions? = null

    abstract suspend fun retrievePermissions(): Permissions

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
            this.permissions = retrievePermissions()

            // Repeat the check! It shouldn't be null now
            hasPermission(*permissions)
        } else {
            if (adminPermissionsBypassesCheck && Permission.Administrator in localPermissions)
                return true

            permissions.all { it in localPermissions }
        }
    }

    class InconsistentCacheException : IllegalStateException()
}