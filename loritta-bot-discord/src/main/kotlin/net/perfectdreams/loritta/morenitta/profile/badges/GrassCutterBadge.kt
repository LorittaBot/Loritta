package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserAchievements
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.util.*

class GrassCutterBadge(val loritta: LorittaBot) : Badge.LorittaBadge(
	UUID.fromString("d8858fec-4075-4308-8494-e7692041cbfa"),
	ProfileDesignManager.I18N_BADGES_PREFIX.GrassCutter.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.GrassCutter.Description,
	"grass_cutter.png",
	15
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return loritta.newSuspendedTransaction {
			UserAchievements.select { UserAchievements.user eq user.id.toLong() and (UserAchievements.type eq AchievementType.STONKS) }
				.count() == 1L
		}
	}
}