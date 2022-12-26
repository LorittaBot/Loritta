package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.cinnamon.pudding.tables.CollectedChristmas2019Points
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import org.jetbrains.exposed.sql.select
import java.util.*

class Christmas2019Badge(val loritta: LorittaBot) : Badge.LorittaBadge(
	UUID.fromString("d9dc5547-9e26-4029-a5e5-4edeed1319c1"),
	ProfileDesignManager.I18N_BADGES_PREFIX.Christmas2019.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.Christmas2019.Description,
	"christmas2019.png",
	100
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return loritta.pudding.transaction {
			CollectedChristmas2019Points.select {
				CollectedChristmas2019Points.user eq profile.id
			}.count() >= 400
		}
	}
}