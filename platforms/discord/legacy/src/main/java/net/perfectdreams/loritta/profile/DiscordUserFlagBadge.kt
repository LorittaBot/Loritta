package net.perfectdreams.loritta.profile

import com.google.gson.JsonElement
import com.mrpowergamerbr.loritta.dao.Profile
import net.dv8tion.jda.api.entities.User

open class DiscordUserFlagBadge(val flag: User.UserFlag, badgeName: String) : Badge(badgeName, 50) {
	class DiscordBraveryHouseBadge : DiscordUserFlagBadge(User.UserFlag.HYPESQUAD_BRAVERY, "badges/discord_bravery.png")
	class DiscordBrillanceHouseBadge : DiscordUserFlagBadge(User.UserFlag.HYPESQUAD_BRILLIANCE, "badges/discord_brilliance.png")
	class DiscordBalanceHouseBadge : DiscordUserFlagBadge(User.UserFlag.HYPESQUAD_BALANCE, "badges/discord_balance.png")
	class DiscordEarlySupporterBadge : DiscordUserFlagBadge(User.UserFlag.EARLY_SUPPORTER,"badges/discord_early_supporter.png")
	class DiscordPartnerBadge : DiscordUserFlagBadge(User.UserFlag.PARTNER,"badges/discord_partner.png")
	class DiscordStaffBadge : DiscordUserFlagBadge(User.UserFlag.STAFF,"badges/discord_partner.png")
	class DiscordHypesquadEventsBadge : DiscordUserFlagBadge(User.UserFlag.HYPESQUAD,"badges/hypesquad_events.png")
	class DiscordVerifiedDeveloperBadge : DiscordUserFlagBadge(User.UserFlag.VERIFIED_DEVELOPER,"badges/verified_developer.png")

	override fun checkIfUserDeservesBadge(user: User, profile: Profile, mutualGuilds: List<JsonElement>) = user.flags.contains(flag)
}