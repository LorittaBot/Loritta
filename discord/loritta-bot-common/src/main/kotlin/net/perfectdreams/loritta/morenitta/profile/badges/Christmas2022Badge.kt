package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.cinnamon.pudding.tables.CollectedChristmas2019Points
import net.perfectdreams.loritta.cinnamon.pudding.tables.christmas2022.CollectedChristmas2022Points
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import org.jetbrains.exposed.sql.select
import java.util.*

class Christmas2022Badge(val loritta: LorittaBot) : Badge.LorittaBadge(
	UUID.fromString("a300e013-be36-44b4-9e0b-f53ba5736744"),
	ProfileDesignManager.I18N_BADGES_PREFIX.Christmas2022.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.Christmas2022.Description,
	"christmas2022.png",
	100
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return loritta.pudding.transaction {
			CollectedChristmas2022Points.select {
				CollectedChristmas2022Points.user eq profile.id
			}.count() >= 100
		}
	}
}