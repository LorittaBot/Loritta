package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.MarriageParticipants
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserMarriages
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.util.*

class MarriedBadge(val pudding: Pudding) : Badge.LorittaBadge(
	UUID.fromString("e5a4c185-6930-4c0a-a14d-194e1383cbe5"),
	ProfileDesignManager.I18N_BADGES_PREFIX.Married.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.Married.TitlePlural,
	ProfileDesignManager.I18N_BADGES_PREFIX.Married.Description,
	"married.png",
	LorittaEmojis.Married,
	190
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		val isMarried = pudding.transaction {
			MarriageParticipants.innerJoin(UserMarriages)
                .selectAll()
				.where {
					MarriageParticipants.user eq user.id and (UserMarriages.expiredAt.isNull())
				}
				.count() != 0L
		}

		return isMarried
	}
}
