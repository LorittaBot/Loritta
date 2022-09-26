package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.utils.config.QuirkyConfig
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.morenitta.profile.Badge

class CanecaBadge(val config: QuirkyConfig) : Badge("badges/lori_caneca.png", 100) {
	override suspend fun checkIfUserDeservesBadge(user: User, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return (user.idLong in config.canecaUsers)
	}
}