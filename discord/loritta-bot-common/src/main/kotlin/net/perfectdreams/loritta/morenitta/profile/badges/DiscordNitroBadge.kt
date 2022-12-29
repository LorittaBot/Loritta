package net.perfectdreams.loritta.morenitta.profile.badges

import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import java.util.*

class DiscordNitroBadge(val loritta: LorittaBot) : Badge.LorittaBadge(
	UUID.fromString("df4f5660-24c6-45a8-82ac-084f8281043a"),
	ProfileDesignManager.I18N_BADGES_PREFIX.DiscordNitro.Title,
	ProfileDesignManager.I18N_BADGES_PREFIX.DiscordNitro.Description,
	"discord_nitro.png",
	50
) {
	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return loritta.pudding.transaction {
			profile.settings.discordPremiumType != null && profile.settings.discordPremiumType != 0
		}
	}
}