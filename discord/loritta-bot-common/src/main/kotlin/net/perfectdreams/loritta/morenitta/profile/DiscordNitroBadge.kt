package net.perfectdreams.loritta.morenitta.profile

import net.dv8tion.jda.api.entities.User
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Profile

class DiscordNitroBadge(val loritta: LorittaBot) : Badge("badges/discord_nitro.png", 50) {
	override suspend fun checkIfUserDeservesBadge(user: User, profile: Profile, mutualGuilds: Set<Long>): Boolean {
		return loritta.pudding.transaction {
			profile.settings.discordPremiumType != null && profile.settings.discordPremiumType != 0
		}
	}
}