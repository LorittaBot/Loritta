package net.perfectdreams.loritta.morenitta.profile.badges

import dev.kord.common.entity.UserFlag
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData

open class DiscordUserFlagBadge(val flag: UserFlag, badgeName: String) : Badge(badgeName, 50) {
    class DiscordBraveryHouseBadge : DiscordUserFlagBadge(UserFlag.HouseBravery, "badges/discord_bravery.png")
    class DiscordBrillanceHouseBadge : DiscordUserFlagBadge(UserFlag.HouseBrilliance, "badges/discord_brilliance.png")
    class DiscordBalanceHouseBadge : DiscordUserFlagBadge(UserFlag.HouseBalance, "badges/discord_balance.png")
    class DiscordEarlySupporterBadge :
        DiscordUserFlagBadge(UserFlag.EarlySupporter, "badges/discord_early_supporter.png")

    class DiscordPartnerBadge : DiscordUserFlagBadge(UserFlag.DiscordPartner, "badges/discord_partner.png")
    class DiscordHypesquadEventsBadge : DiscordUserFlagBadge(UserFlag.HypeSquad, "badges/hypesquad_events.png")
    class DiscordVerifiedDeveloperBadge :
        DiscordUserFlagBadge(UserFlag.VerifiedBotDeveloper, "badges/verified_developer.png")

    override suspend fun checkIfUserDeservesBadge(
        user: ProfileUserInfoData,
        profile: Profile,
        mutualGuilds: Set<Long>
    ) = user.flags.contains(flag)
}