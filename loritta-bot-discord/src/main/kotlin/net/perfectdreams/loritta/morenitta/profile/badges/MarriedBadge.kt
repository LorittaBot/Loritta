package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.utils.config.FanArtArtist
import java.io.File
import java.util.*
import javax.imageio.ImageIO

class MarriedBadge(val loritta: LorittaBot) : Badge.LorittaBadge(
	UUID.fromString("e5a4c185-6930-4c0a-a14d-194e1383cbe5"),
	ProfileDesignManager.I18N_BADGES_PREFIX.Married.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.Married.Description,
	"married.png",
	190
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		val marriage = loritta.newSuspendedTransaction { profile.marriage }
		return marriage != null
	}
}