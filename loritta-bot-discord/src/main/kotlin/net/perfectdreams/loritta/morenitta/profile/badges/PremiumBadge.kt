package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import java.util.*

class PremiumBadge(val loritta: LorittaBot) : Badge.LorittaBadge(
	UUID.fromString("2ef616dd-dd0e-4a17-98e1-10a6bfa7d6a6"),
	ProfileDesignManager.I18N_BADGES_PREFIX.Premium.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.Premium.Description,
	"donator.png",
	200
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return loritta.getActiveMoneyFromDonations(user.id.toLong()) != 0.0
	}
}