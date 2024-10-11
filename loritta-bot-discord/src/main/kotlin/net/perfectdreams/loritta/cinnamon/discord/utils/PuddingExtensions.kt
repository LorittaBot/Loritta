/**
 * File containing Pudding extensions, mostly extensions that has JDA references
 */
package net.perfectdreams.loritta.cinnamon.discord.utils

import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.cinnamon.pudding.services.UsersService
import net.perfectdreams.loritta.serializable.UserId

suspend fun UsersService.getUserProfile(user: User) = getUserProfile(UserId(user.idLong))
suspend fun UsersService.getOrCreateUserProfile(user: User) = getOrCreateUserProfile(UserId(user.idLong))

suspend fun UsersService.getUserAchievements(user: User) = getUserAchievements(UserId(user.idLong))