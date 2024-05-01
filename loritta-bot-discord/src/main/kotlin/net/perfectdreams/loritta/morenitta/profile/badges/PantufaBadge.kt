package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.cinnamon.discord.utils.toLong
import net.perfectdreams.loritta.cinnamon.pudding.Pudding
import net.perfectdreams.loritta.cinnamon.pudding.tables.Birthday2020Players
import net.perfectdreams.loritta.cinnamon.pudding.tables.CollectedBirthday2020Points
import net.perfectdreams.loritta.cinnamon.pudding.utils.BirthdayTeam
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import org.jetbrains.exposed.sql.select
import java.util.*

class PantufaBadge(val pudding: Pudding) : Badge.LorittaBadge(
	UUID.fromString("5196d860-e8a6-4781-a22f-6bfa26c0af7c"),
	ProfileDesignManager.I18N_BADGES_PREFIX.Pantufa2020.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.Pantufa2020.Description,
	"birthday2020_pantufa.png",
	100
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		val playerResult = pudding.transaction {
			Birthday2020Players.select { Birthday2020Players.user eq profile.id }
					.firstOrNull()
		} ?: return false

		if (playerResult[Birthday2020Players.team] != BirthdayTeam.PANTUFA)
			return false

		val count = pudding.transaction {
			CollectedBirthday2020Points.select {
				CollectedBirthday2020Points.user eq user.id.toLong()
			}.count()
		}

		return count >= 100
	}
}