package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserAchievements
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.util.*

class StonksBadge(val pudding: Pudding) : Badge.LorittaBadge(
	UUID.fromString("4c029e28-95ec-479e-9570-1ad9dab32816"),
	ProfileDesignManager.I18N_BADGES_PREFIX.Stonks.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.Stonks.Description,
	"stonks.png",
	15
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return pudding.transaction {
			UserAchievements.select { UserAchievements.user eq user.id.toLong() and (UserAchievements.type eq AchievementType.GRASS_CUTTER) }
				.count() == 1L
		}
	}
}