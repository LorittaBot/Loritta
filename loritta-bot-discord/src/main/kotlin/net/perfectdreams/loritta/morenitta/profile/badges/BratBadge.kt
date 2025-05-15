package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.BackgroundPayments
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import org.jetbrains.exposed.sql.and
import java.util.*

class BratBadge(val pudding: Pudding) : Badge.LorittaBadge(
	UUID.fromString("3e95aa2d-b092-43dc-8aef-7d2112e13f28"),
	ProfileDesignManager.I18N_BADGES_PREFIX.Brat.Title,
	null,
	ProfileDesignManager.I18N_BADGES_PREFIX.Brat.Description,
	"brat.png",
	LorittaEmojis.Brat,
	50
) {
	companion object {
		private val BRAT_BACKGROUNDS = listOf(
			"charliXcxBratLori",
			"charliXcxBrat",
			"charliXcxBratPantufa",
			"charliXcxBratGabi",
			"charliXcxBratPirralha",
			"charliXcxBratRawr",
			"charliXcxBratCoy",
			"charliXcxBratOwo",
			"charliXcxBratUwu"
		)
	}

	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return pudding.transaction {
			BackgroundPayments.select(BackgroundPayments.background)
				.where {
					BackgroundPayments.background inList BRAT_BACKGROUNDS and (BackgroundPayments.userId eq user.id)
				}
				.toList()
				.map {
					it[BackgroundPayments.background].value
				}
				.containsAll(BRAT_BACKGROUNDS)
		}
	}
}
