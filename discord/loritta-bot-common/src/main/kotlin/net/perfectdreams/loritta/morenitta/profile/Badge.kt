package net.perfectdreams.loritta.morenitta.profile

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.dv8tion.jda.api.entities.User

abstract class Badge(val badgeFileName: String, val priority: Int) {
	abstract suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean
}