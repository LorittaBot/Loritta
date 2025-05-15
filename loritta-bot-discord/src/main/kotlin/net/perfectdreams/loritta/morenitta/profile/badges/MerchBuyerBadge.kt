package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import java.util.*

class MerchBuyerBadge(val m: LorittaBot) : Badge.LorittaBadge(
	UUID.fromString("00b2b958-55bd-4daa-8822-eafd29b933ca"),
	ProfileDesignManager.I18N_BADGES_PREFIX.MerchBuyer.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.MerchBuyer.Description,
	"lori_caneca.png",
	LorittaEmojis.LoriCaneca,
	225
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return m.cachedGabrielaHelperMerchBuyerIdsResponse?.contains(user.id.toLong()) == true
	}
}
