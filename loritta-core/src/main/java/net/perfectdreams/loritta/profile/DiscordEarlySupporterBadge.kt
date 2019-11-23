package net.perfectdreams.loritta.profile

import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.transactions.transaction

class DiscordEarlySupporterBadge : Badge("badges/discord_early_supporter.png", 50) {
	override fun checkIfUserDeservesBadge(user: User, profile: Profile, mutualGuilds: List<JsonElement>): Boolean {
		val shiftedFlag = 1 shl 9

		return transaction(Databases.loritta) {
			(profile.settings.discordAccountFlags and shiftedFlag) == shiftedFlag
		}
	}
}