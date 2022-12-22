package net.perfectdreams.loritta.morenitta.profile.badges

import dev.kord.common.entity.UserFlag
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData

open class DiscordUserFlagBadge(val flag: UserFlag, badgeName: String) : Badge(badgeName, 50) {
	class DiscordBraveryHouseBadge : DiscordUserFlagBadge(UserFlag.HouseBravery, "discord_bravery.png")
	class DiscordBrillanceHouseBadge : DiscordUserFlagBadge(UserFlag.HouseBrilliance, "discord_brillance.png")
	class DiscordBalanceHouseBadge : DiscordUserFlagBadge(UserFlag.HouseBalance, "discord_balance.png")
	class DiscordEarlySupporterBadge : DiscordUserFlagBadge(UserFlag.EarlySupporter,"discord_early_supporter.png")
	class DiscordPartnerBadge : DiscordUserFlagBadge(UserFlag.DiscordPartner,"discord_partner.png")
	class DiscordHypesquadEventsBadge : DiscordUserFlagBadge(UserFlag.HypeSquad,"hypesquad_events.png")
	class DiscordVerifiedDeveloperBadge : DiscordUserFlagBadge(UserFlag.VerifiedBotDeveloper,"verified_developer.png")

	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>) = user.flags.contains(flag)
}