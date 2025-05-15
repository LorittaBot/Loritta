package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.morenitta.dao.Profile
import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.cinnamon.pudding.tables.Birthday2020Players
import net.perfectdreams.loritta.cinnamon.pudding.tables.CollectedBirthday2020Points
import net.perfectdreams.loritta.cinnamon.pudding.utils.BirthdayTeam
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import net.perfectdreams.loritta.morenitta.utils.Constants
import org.jetbrains.exposed.sql.select
import java.util.*

class SparklyStaffBadge(val loritta: LorittaBot) : Badge.LorittaBadge(
	UUID.fromString("54839157-767a-425f-8cf2-5ebfab4f9c13"),
	ProfileDesignManager.I18N_BADGES_PREFIX.SparklyStaff.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.SparklyStaff.Description,
	"sparkly_staff.png",
	LorittaEmojis.SparklyStaff,
	900
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		// TODO: It would be better if there was an API instead of relying if the user has the role in the server
		return loritta.profileDesignManager.hasRole(
			user.id.toLong(),
			Constants.SPARKLYPOWER_GUILD_ID,
				332650495522897920L, // "Staff" role
		)
	}
}
