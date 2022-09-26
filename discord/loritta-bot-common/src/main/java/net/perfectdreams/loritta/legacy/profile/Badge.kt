package net.perfectdreams.loritta.legacy.profile

import net.perfectdreams.loritta.legacy.dao.Profile
import net.dv8tion.jda.api.entities.User

abstract class Badge(val badgeFileName: String, val priority: Int) {
	abstract fun checkIfUserDeservesBadge(user: User, profile: Profile, mutualGuilds: Set<Long>): Boolean
}