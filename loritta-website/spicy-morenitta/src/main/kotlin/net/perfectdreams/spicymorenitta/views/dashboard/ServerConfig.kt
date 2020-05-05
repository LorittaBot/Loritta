package net.perfectdreams.spicymorenitta.views.dashboard

import kotlinx.serialization.Optional
import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.utils.ServerPremiumPlans
import utils.LoriColor

object ServerConfig {
	@Serializable
	class MiniGuild(
			val id: Long,
			val name: String,
			@Optional val iconUrl: String? = null
	)

	@Serializable
	class GeneralConfig(
			// É deserializado para String pois JavaScript é burro e não funciona direito com Longs
			val localeId: String,
			val commandPrefix: String,
			val deleteMessageAfterCommand: Boolean,
			val warnOnUnknownCommand: Boolean,
			val blacklistedChannels: Array<Long>,
			val warnIfBlacklisted: Boolean,
			@Optional
			val blacklistedWarning: String? = null
	)

	@Serializable
	class Guild(
			// É deserializado para String pois JavaScript é burro e não funciona direito com Longs
			val name: String,
			val general: GeneralConfig,
			val selfMember: SelfMember,
			@Optional val donationKey: DonationKey? = null,
			val donationConfig: DonationConfig,
			val reactionRoleConfigs: List<ReactionOption>,
			val levelUpConfig: LevelUpConfig,
			val trackedTwitterAccounts: Array<TrackedTwitterAccount>,
			val trackedYouTubeChannels: Array<TrackedYouTubeAccount>,
			val trackedTwitchChannels: Array<TrackedTwitchAccount>,
			val trackedRssFeeds: Array<TrackedRssFeed>,
			val roles: Array<Role>,
			val textChannels: Array<TextChannel>
	)

	@Serializable
	class SelfMember(
			val id: Long,
			val name: String,
			val discriminator: String,
			val effectiveAvatarUrl: String,
			@Optional
			val donationKeys: List<DonationKey>? = null
	)

	@Serializable
	class DonationKey(
			val id: Long,
			val value: Double,
			@Optional val user: SelfMember? = null,
			val expiresAt: Long,
			@Optional val activeIn: MiniGuild? = null
	)

	@Serializable
	class DonationConfig(
			val customBadge: Boolean,
			val dailyMultiplier: Boolean
	)

	@Serializable
	class MiscellaneousConfig(
			val enableQuirky: Boolean,
			val enableBomDiaECia: Boolean
	)

	@Serializable
	class MemberCounterConfig(
			val channelId: Long,
			var topic: String,
			var theme: String,
			var padding: Int
	)

	@Serializable
	class ModerationConfig(
		var sendPunishmentViaDm: Boolean = false,
		var sendPunishmentToPunishLog: Boolean = false,
		@Optional var punishmentLogChannelId: Long? = null,
		@Optional var punishmentLogMessage: String? = null,
		var punishmentActions: Array<WarnAction>
	)

	@Serializable
	class WarnAction(
			var warnCount: Int,
			var punishmentAction: PunishmentAction,
			@Optional var customMetadata0: String? = null
	)

	enum class PunishmentAction {
		BAN,
		SOFT_BAN,
		KICK,
		MUTE
	}

	@Serializable
	class WelcomerConfig(
			@Optional val enabled: Boolean = true,
			val tellOnJoin: Boolean,
			var tellOnRemove: Boolean,
			@Optional var joinMessage: String? = null,
			@Optional var removeMessage: String? = null,
			@Optional var channelJoinId: Long? = null,
			@Optional var channelRemoveId: Long? = null,
			var tellOnPrivateJoin: Boolean = false,
			@Optional var joinPrivateMessage: String? = null,
			var tellOnBan: Boolean,
			@Optional var bannedMessage: String? = null,
			@Optional val deleteJoinMessagesAfter: Long? = null,
			@Optional val deleteRemoveMessagesAfter: Long? = null
	)

	@Serializable
	class MusicConfig(
			val enabled: Boolean,
			val channels: List<Long>
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
	class FortniteConfig(
			val advertiseNewItems: Boolean,
			@Optional val channelToAdvertiseNewItems: Long? = null
	)

	@Serializable
	class Announcement(
			val type: String,
			@Optional val channelId: Long? = null,
			val onlyIfUserReceivedRoles: Boolean,
			val message: String
	)

	@Serializable
	class RoleByExperience(
			val requiredExperience: String,
			val roles: List<Long>
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
	class TrackedYouTubeAccount(
			val channelId: Long,
			val youTubeChannelId: String,
			val message: String,
			@Optional val webhookUrl: String? = null
	)

	@Serializable
	class TrackedTwitchAccount(
			val channelId: Long,
			val twitchUserId: Long,
			val message: String,
			@Optional val webhookUrl: String? = null
	)

	@Serializable
	class TrackedRssFeed(
			val channelId: Long,
			val feedUrl: String,
			val message: String
	)

	@Serializable
	class AutoroleConfig(
			var enabled: Boolean = false,
			var roles: Array<Long>,
			@Optional var giveRolesAfter: Long? = null,
			var giveOnlyAfterMessageWasSent: Boolean
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
			val id: Long,
			val channelId: Long,
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
			val id: Long,
			val name: String,
			val colorRaw: Int,
			val canInteract: Boolean,
			val isManaged: Boolean,
			val isHoisted: Boolean,
			val isPublicRole: Boolean
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
			val id: Long,
			val name: String,
			val canTalk: Boolean,
			@Optional val topic: String? = null
	)

	@Serializable
	class VoiceChannel(
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

fun Collection<ServerConfig.DonationKey>.getValue() = this.sumByDouble { it.value }
fun Collection<ServerConfig.DonationKey>.getPlan() = ServerPremiumPlans.getPlanFromValue(this.getValue())