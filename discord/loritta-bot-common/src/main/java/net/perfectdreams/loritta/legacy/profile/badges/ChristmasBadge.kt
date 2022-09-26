package net.perfectdreams.loritta.legacy.profile.badges

import net.perfectdreams.loritta.legacy.dao.Profile
import net.perfectdreams.loritta.legacy.network.Databases
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.legacy.profile.Badge
import net.perfectdreams.loritta.cinnamon.pudding.tables.CollectedChristmas2019Points
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class ChristmasBadge : Badge("christmas2019_badge.png", 100) {
	override fun checkIfUserDeservesBadge(user: User, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return transaction(Databases.loritta) {
			CollectedChristmas2019Points.select {
				CollectedChristmas2019Points.user eq profile.id
			}.count() >= 400
		}
	}
}