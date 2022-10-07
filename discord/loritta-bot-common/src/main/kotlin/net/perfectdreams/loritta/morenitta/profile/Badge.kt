package net.perfectdreams.loritta.morenitta.profile

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.deviousfun.entities.User

abstract class Badge(val badgeFileName: String, val priority: Int) {
	abstract suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean
}