package net.perfectdreams.loritta.profile

import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.transactions.transaction

class DiscordNitroBadge : Badge("badges/discord_nitro.png", 50) {
	override fun checkIfUserDeservesBadge(user: User, profile: Profile, mutualGuilds: List<JsonElement>): Boolean {
		return transaction(Databases.loritta) {
			profile.settings.discordPremiumType != null && profile.settings.discordPremiumType != 0
		}
	}
}