package net.perfectdreams.loritta.cinnamon.discord.utils.entitycache

import dev.kord.common.entity.Permission
import dev.kord.common.entity.Permissions
import dev.kord.common.entity.Snowflake
import dev.kord.rest.service.RestClient
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.discord.utils.metrics.CinnamonMetrics
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.utils.exposed.disableSynchronousCommit

/**
 * Lazy and Cached permission checks
 */
class LazyCachedPermissions internal constructor(
    private val rest: RestClient,
    private val pudding: Pudding,
    private val cacheService: DiscordCacheService,
    private val guildId: Snowflake,
    private val channelId: Snowflake,
    private val userId: Snowflake
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

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
            var permissionsResult = cacheService.getPermissions(guildId, channelId, userId)

            // We could also check for permissionsResult.notInGuild, but that's harder because maybe the user really *isn't* in the guild
            // TODO: Maybe add a guild member refresh as an option?
            if (permissionsResult.missingChannels || permissionsResult.missingRoles) {
                logger.warn { "Cache inconsistency detected in $guildId -> $channelId for $userId! ($permissionsResult) We will pull the updated data from Discord's REST and update the cache... Let's hope for the best... :S" }

                logger.warn { "Pulling $guildId's information via REST to update the inconsistent cache..." }
                val guild = rest.guild.getGuild(guildId)

                logger.warn { "Pulling all channels in $guildId via REST to update the inconsistent cache..." }
                val channels = rest.guild.getGuildChannels(guildId)

                logger.warn { "Updating inconsistent cache of $guildId..." }

                pudding.transaction {
                    disableSynchronousCommit()

                    cacheService.createOrUpdateGuild(
                        guildId,
                        guild.name,
                        guild.icon,
                        guild.ownerId,
                        guild.roles,
                        channels,
                        guild.emojis
                    )
                }

                logger.warn { "Successfully updated inconsistent cache of $guildId! We will now try checking for the permissions again and, if it fails, we will throw an exception..." }

                permissionsResult = cacheService.getPermissions(guildId, channelId, userId)

                if (permissionsResult.missingChannels || permissionsResult.missingRoles) { // bye
                    logger.warn { "Something went very wrong here... The cache of $guildId is still inconsistent! ($permissionsResult) Throwing InconsistentCacheException..." }
                    CinnamonMetrics.permissionsCacheInconsistencyFixed
                        .labels("failure")
                        .inc()
                    throw InconsistentCacheException()
                }

                CinnamonMetrics.permissionsCacheInconsistencyFixed
                    .labels("success")
                    .inc()

                logger.info { "Inconsistent cache of $guildId was successfully updated and validated and it seems to be working fine :3" }
            }

            this.permissions = permissionsResult.permissions

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