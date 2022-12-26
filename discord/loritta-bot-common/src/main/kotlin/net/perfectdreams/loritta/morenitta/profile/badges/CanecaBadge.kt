package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.utils.config.LorittaConfig
import java.util.*

class CanecaBadge(val config: LorittaConfig.QuirkyConfig) : Badge.LorittaBadge(
	UUID.fromString("81788d4a-7e6c-415f-8832-d55573f8c40b"),
	ProfileDesignManager.I18N_BADGES_PREFIX.MerchBuyer.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.MerchBuyer.Description,
	"lori_caneca.png",
	100
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return (user.id in config.canecaUsers)
	}
}