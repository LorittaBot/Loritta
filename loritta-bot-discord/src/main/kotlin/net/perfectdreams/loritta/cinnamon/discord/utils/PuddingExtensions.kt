/**
 * File containing Pudding extensions, mostly extensions that has Kord references
 */
package net.perfectdreams.loritta.cinnamon.discord.utils

import dev.kord.common.entity.DiscordGuildMember
import dev.kord.common.entity.Snowflake
import dev.kord.core.entity.User
import net.perfectdreams.loritta.common.utils.LorittaPermission
import net.perfectdreams.loritta.serializable.UserId
import net.perfectdreams.loritta.cinnamon.pudding.services.ServerConfigsService
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService

fun UserId(snowflake: Snowflake) = UserId(snowflake.value)

fun Snowflake.toLong() = this.value.toLong()

suspend fun UsersService.getUserProfile(user: User) = getUserProfile(UserId(user.id.value))
suspend fun UsersService.getOrCreateUserProfile(user: User) = getOrCreateUserProfile(UserId(user.id.value))

suspend fun UsersService.getUserAchievements(user: User) = getUserAchievements(UserId(user.id.value))

suspend fun ServerConfigsService.hasLorittaPermission(guildId: Snowflake, roleIds: List<Snowflake>, vararg permission: LorittaPermission) = hasLorittaPermission(
    guildId.value,
    roleIds.map { it.value },
    *permission
)

suspend fun ServerConfigsService.hasLorittaPermission(guildId: Snowflake, member: DiscordGuildMember, vararg permission: LorittaPermission) = hasLorittaPermission(
    guildId.value,
    member.roles.map { it.value },
    *permission
)