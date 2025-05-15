package net.perfectdreams.loritta.morenitta.profile.badges

import net.dv8tion.jda.api.entities.User.UserFlag
import net.perfectdreams.i18nhelper.core.keydata.StringI18nData
import net.perfectdreams.loritta.common.emojis.LorittaEmojiReference
import net.perfectdreams.loritta.common.emojis.LorittaEmojis
import net.perfectdreams.loritta.morenitta.dao.Profile
import net.perfectdreams.loritta.morenitta.profile.Badge
import net.perfectdreams.loritta.morenitta.profile.ProfileDesignManager
import net.perfectdreams.loritta.morenitta.profile.ProfileUserInfoData
import java.util.*

open class DiscordUserFlagBadge(
	val flag: UserFlag,
	id: UUID,
	title: StringI18nData,
	titlePlural: StringI18nData?,
	description: StringI18nData,
	badgeName: String,
	emoji: LorittaEmojiReference,
) : Badge.LorittaBadge(id, title, titlePlural, description, badgeName, emoji, 0) {
	class DiscordBraveryHouseBadge : DiscordUserFlagBadge(
		UserFlag.HYPESQUAD_BRAVERY,
		UUID.fromString("4415250e-0a5f-40b9-bd0f-caa8d97b55e3"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBravery.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBravery.TitlePlural,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBravery.Description,
		"discord_bravery.png",
		LorittaEmojis.DiscordBraveryHouse
	)
	class DiscordBrillianceHouseBadge : DiscordUserFlagBadge(
		UserFlag.HYPESQUAD_BRILLIANCE,
		UUID.fromString("1812c988-0eec-405e-8670-d5419ccb1fe8"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBrilliance.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBrilliance.TitlePlural,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBrilliance.Description,
		"discord_brilliance.png",
		LorittaEmojis.DiscordBrillianceHouse
	)
	class DiscordBalanceHouseBadge : DiscordUserFlagBadge(
		UserFlag.HYPESQUAD_BALANCE,
		UUID.fromString("7e8973ff-65be-4941-afb9-8df6b8febfc9"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBalance.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBalance.TitlePlural,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordBalance.Description,
		"discord_balance.png",
		LorittaEmojis.DiscordBalanceHouse
	)
	class DiscordEarlySupporterBadge : DiscordUserFlagBadge(
		UserFlag.EARLY_SUPPORTER,
		UUID.fromString("3349d275-390f-40c2-83dc-8245bd530f0e"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordEarlySupporter.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordEarlySupporter.TitlePlural,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordEarlySupporter.Description,
		"discord_early_supporter.png",
		LorittaEmojis.DiscordEarlySupporter
	)
	class DiscordPartnerBadge : DiscordUserFlagBadge(
		UserFlag.PARTNER,
		UUID.fromString("97004586-188f-4d4a-bba8-53b7fd5e0a9a"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordPartner.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordPartner.TitlePlural,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordPartner.Description,
		"discord_partner.png",
		LorittaEmojis.DiscordPartner
	)
	class DiscordHypesquadEventsBadge : DiscordUserFlagBadge(
		UserFlag.HYPESQUAD,
		UUID.fromString("f5665d18-ff6d-4660-be07-ac0aa1188447"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordHypesquadEvents.Title,
		null,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordHypesquadEvents.Description,
		"hypesquad_events.png",
		LorittaEmojis.DiscordHypesquadEvents
	)
	class DiscordVerifiedDeveloperBadge : DiscordUserFlagBadge(
		UserFlag.VERIFIED_DEVELOPER,
		UUID.fromString("3e8fc05e-490f-4533-bd39-af7f81a81867"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordVerifiedDeveloper.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordVerifiedDeveloper.TitlePlural,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordVerifiedDeveloper.Description,
		"verified_developer.png",
		LorittaEmojis.DiscordVerifiedDeveloper
	)
	class DiscordActiveDeveloperBadge : DiscordUserFlagBadge(
		UserFlag.ACTIVE_DEVELOPER,
		UUID.fromString("64b11eb7-733c-419a-9cbc-34cf5427c805"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordActiveDeveloper.Title,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordActiveDeveloper.TitlePlural,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordActiveDeveloper.Description,
		"active_developer.png",
		LorittaEmojis.DiscordActiveDeveloper
	)
	class DiscordModeratorProgramAlumniBadge : DiscordUserFlagBadge(
		UserFlag.CERTIFIED_MODERATOR,
		UUID.fromString("edb16ae5-ff96-4872-a5df-c70eb0cac766"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordModeratorProgramAlumni.Title,
		null,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordModeratorProgramAlumni.Description,
		"moderator_program_alumni.png",
		LorittaEmojis.DiscordModeratorProgramAlumni
	)
	class DiscordStaffBadge : DiscordUserFlagBadge(
		UserFlag.STAFF,
		UUID.fromString("ace5644a-384e-458e-a3a1-20e99fdb299e"),
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordStaff.Title,
		null,
		ProfileDesignManager.I18N_BADGES_PREFIX.DiscordStaff.Description,
		"discord_staff.png",
		LorittaEmojis.DiscordStaff
	)

	override suspend fun checkIfUserDeservesBadge(user: ProfileUserInfoData, profile: Profile, mutualGuilds: Set<Long>) = user.flags.contains(flag)
}
