package net.perfectdreams.loritta.legacy.profile.badges

import net.perfectdreams.loritta.legacy.dao.Profile
import net.perfectdreams.loritta.legacy.utils.config.QuirkyConfig
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.legacy.profile.Badge

class CanecaBadge(val config: QuirkyConfig) : Badge("badges/lori_caneca.png", 100) {
	override fun checkIfUserDeservesBadge(user: User, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return (user.idLong in config.canecaUsers)
	}
}