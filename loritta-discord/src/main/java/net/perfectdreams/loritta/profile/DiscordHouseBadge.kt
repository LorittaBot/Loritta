package net.perfectdreams.loritta.profile

import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.dao.Profile
import com.mrpowergamerbr.loritta.network.Databases
import net.dv8tion.jda.api.entities.User
import org.jetbrains.exposed.sql.transactions.transaction

abstract class DiscordHouseBadge(val shift: Int, badgeName: String) : Badge(badgeName, 50) {
	class DiscordBraveryHouseBadge : DiscordHouseBadge(6, "badges/discord_bravery.png")
	class DiscordBrillanceHouseBadge : DiscordHouseBadge(7, "badges/discord_brilliance.png")
	class DiscordBalanceHouseBadge : DiscordHouseBadge(8, "badges/discord_balance.png")

	override fun checkIfUserDeservesBadge(user: User, profile: Profile, mutualGuilds: List<JsonElement>): Boolean {
		val shiftedFlag = 1 shl shift

		return transaction(Databases.loritta) {
			(profile.settings.discordAccountFlags and shiftedFlag) == shiftedFlag
		}
	}
}