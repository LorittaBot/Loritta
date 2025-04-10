package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import java.util.*

class SuperPremiumBadge(val loritta: LorittaBot) : Badge.LorittaBadge(
	UUID.fromString("fa286d07-6e55-473a-9aec-84f6c6337a02"),
	ProfileDesignManager.I18N_BADGES_PREFIX.SuperPremium.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.SuperPremium.Description,
	"super_donator.png",
	250
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return loritta.getActiveMoneyFromDonations(user.id.toLong()) >= 99.0
	}
}