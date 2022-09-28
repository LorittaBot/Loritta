package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.cinnamon.pudding.tables.CollectedChristmas2019Points
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import org.jetbrains.exposed.sql.select

class ChristmasBadge(val loritta: LorittaBot) : Badge("christmas2019_badge.png", 100) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return loritta.pudding.transaction {
			CollectedChristmas2019Points.select {
				CollectedChristmas2019Points.user eq profile.id
			}.count() >= 400
		}
	}
}