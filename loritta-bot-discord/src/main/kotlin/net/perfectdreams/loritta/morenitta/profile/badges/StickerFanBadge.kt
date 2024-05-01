package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserAchievements
import net.perfectdreams.loritta.cinnamon.pudding.tables.loricoolcards.LoriCoolCardsFinishedAlbumUsers
import net.perfectdreams.loritta.common.achievements.AchievementType
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import java.util.*

class StickerFanBadge(val pudding: Pudding) : Badge.LorittaBadge(
	UUID.fromString("d5e6b7be-5bd4-4822-b81e-1285427f208f"),
	ProfileDesignManager.I18N_BADGES_PREFIX.StickerFan.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.StickerFan.Description,
	"sticker_fan.png",
	100
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return pudding.transaction {
			LoriCoolCardsFinishedAlbumUsers.select { LoriCoolCardsFinishedAlbumUsers.user eq user.id }
				.count() == 1L
		}
	}
}