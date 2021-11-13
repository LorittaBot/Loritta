package net.perfectdreams.loritta.profile.badges

import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.dao.Profile
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.QuirkyConfig
import net.perfectdreams.loritta.profile.Badge

class CanecaBadge(val config: QuirkyConfig) : Badge("badges/lori_caneca.png", 100) {
	override fun checkIfUserDeservesBadge(user: User, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return (user.idLong in config.canecaUsers)
	}
}