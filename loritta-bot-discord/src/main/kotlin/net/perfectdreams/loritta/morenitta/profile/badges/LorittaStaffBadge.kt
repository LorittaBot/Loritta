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

class LorittaStaffBadge(val loritta: LorittaBot) : Badge.LorittaBadge(
	UUID.fromString("8f707b11-55cb-4b8c-aae3-62e382e268fc"),
	ProfileDesignManager.I18N_BADGES_PREFIX.LorittaStaff.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.LorittaStaff.Description,
	"loritta_staff.png",
	LorittaEmojis.LorittaStaff,
	1000
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		// TODO: It would be better if there was an API instead of relying if the user has the role in the server
		return loritta.profileDesignManager.hasRole(
			user.id.toLong(),
			Constants.PORTUGUESE_SUPPORT_GUILD_ID,
			399301696892829706L, // "Suporte da Lori" role
		)
	}
}
