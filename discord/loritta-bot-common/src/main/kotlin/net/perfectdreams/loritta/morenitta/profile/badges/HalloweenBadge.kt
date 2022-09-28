package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.cinnamon.pudding.tables.CollectedCandies
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class HalloweenBadge(val loritta: LorittaBot) : Badge("halloween2019_badge.png", 100) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return loritta.pudding.transaction {
			CollectedCandies.select {
				CollectedCandies.user eq profile.id
			}.count() >= 400L
		}
	}
}