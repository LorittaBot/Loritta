package net.perfectdreams.loritta.profile.badges

import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.Halloween2019
import net.perfectdreams.loritta.profile.Badge
import net.perfectdreams.loritta.tables.CollectedCandies
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class HalloweenBadge : Badge("halloween2019_badge.png", 100) {
	override fun checkIfUserDeservesBadge(user: User, profile: Profile, mutualGuilds: List<JsonElement>): Boolean {
		return transaction(Databases.loritta) {
			CollectedCandies.select {
				CollectedCandies.user eq profile.id
			}.count() >= Halloween2019.BADGE
		}
	}
}