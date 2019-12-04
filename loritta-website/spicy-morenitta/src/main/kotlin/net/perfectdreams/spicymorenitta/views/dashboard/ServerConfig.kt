package net.perfectdreams.spicymorenitta.views.dashboard

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import utils.LoriColor

object ServerConfig {
	@Serializable
	class MiniGuild(
			val name: String,
			@Optional val iconUrl: String? = null
	)

	@Serializable
	class Guild(
			// É deserializado para String pois JavaScript é burro e não funciona direito com Longs
			val name: String,
			val selfMember: SelfMember,
			@Optional val donationKey: DonationKey? = null,
			val donationConfig: DonationConfig,
			val reactionRoleConfigs: List<ReactionOption>,
			val levelUpConfig: LevelUpConfig,
			val trackedTwitterAccounts: Array<TrackedTwitterAccount>,
			val trackedRssFeeds: Array<TrackedRssFeed>,
			val roles: Array<Role>,
			val textChannels: Array<TextChannel>
	)

	@Serializable
	class SelfMember(
			val id: String,
			val name: String,
			val discriminator: String,
			val effectiveAvatarUrl: String,
			@Optional
			val donationKeys: List<DonationKey>? = null
	)

	@Serializable
	class DonationKey(
			val id: String,
			val value: Double,
			@Optional val user: SelfMember? = null,
			val expiresAt: Long,
			@Optional val usesKey: MiniGuild? = null
	)

	@Serializable
	class DonationConfig(
			val customBadge: Boolean,
			val dailyMultiplier: Boolean
	)

	@Serializable
	class LevelUpConfig(
			val roleGiveType: String,
			val noXpRoles: List<Long>,
			val noXpChannels: List<Long>,
			val announcements: List<Announcement>,
			val rolesByExperience: List<RoleByExperience>,
			val experienceRoleRates: List<ExperienceRoleRate>
	)

	@Serializable
	class Announcement(
			val type: String,
			@Optional val channelId: String? = null,
			val onlyIfUserReceivedRoles: Boolean,
			val message: String
	)

	@Serializable
	class RoleByExperience(
			val requiredExperience: String,
			val roles: List<String>
	)

	@Serializable
	class ExperienceRoleRate(
			val role: Long,
			val rate: Double
	)

	@Serializable
	class TrackedTwitterAccount(
			val channelId: Long,
			val twitterAccountId: Long,
			val message: String
	)

	@Serializable
	class TrackedRssFeed(
			val channelId: Long,
			val feedUrl: String,
			val message: String
	)

	@Serializable
	class ReactionOption(
			val textChannelId: String,
			val messageId: String,
			val reaction: String,
			val locks: Array<String>,
			val roleIds: Array<String>
	)

	@Serializable
	class DiscordMessage(
			val id: String,
			val channelId: String,
			val content: String,
			val reactions: Array<DiscordReaction>
	)

	@Serializable
	class DiscordReaction(
			val isDiscordEmote: Boolean,
			val name: String,
			@Optional val id: String? = null
	)

	@Serializable
	class Role(
			val id: String,
			val name: String,
			val colorRaw: Int,
			val canInteract: Boolean,
			val isManaged: Boolean,
			val isHoisted: Boolean
	) {
		fun getColor(): LoriColor? {
			if (colorRaw == 0x1FFFFFFF)
				return null

			val red = colorRaw shr 16 and 0x000000FF
			val green = colorRaw shr 8 and 0x000000FF
			val blue = colorRaw and 0x000000FF

			return LoriColor(red, green, blue)
		}
	}

	@Serializable
	class TextChannel(
			val id: String,
			val name: String
	)

	@Serializable
	class WebAuditLogWrapper(
			val users: List<SelfMember>,
			val entries: List<WebAuditLogEntry>
	)

	@Serializable
	class WebAuditLogEntry(
			val id: Long,
			val executedAt: Long,
			val type: String
	)
}