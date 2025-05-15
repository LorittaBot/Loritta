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

class SparklyMemberBadge(val loritta: LorittaBot) : Badge.LorittaBadge(
	UUID.fromString("7facf3a6-9f29-4841-9eb0-b27312a0ceb1"),
	ProfileDesignManager.I18N_BADGES_PREFIX.SparklyMember.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.SparklyMember.TitlePlural,
	ProfileDesignManager.I18N_BADGES_PREFIX.SparklyMember.Description,
	"sparkly_member.png",
	LorittaEmojis.SparklyMember,
	15
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		// TODO: It would be better if there was an API instead of relying if the user has the role in the server
		return loritta.profileDesignManager.hasRole(
			user.id.toLong(),
			Constants.SPARKLYPOWER_GUILD_ID,
			393468942959509507L, // "Membro do SparklyPower" role
		)
	}
}
