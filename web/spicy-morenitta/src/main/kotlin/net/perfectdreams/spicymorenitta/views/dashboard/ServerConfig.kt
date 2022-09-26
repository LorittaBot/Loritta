package net.perfectdreams.spicymorenitta.views.dashboard

import kotlinx.serialization.Serializable
import net.perfectdreams.loritta.common.utils.ServerPremiumPlans
import utils.LoriColor

object ServerConfig {
	@Serializable
	class MiniGuild(
			val id: Long,
			val name: String,
			val iconUrl: String? = null
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

			val blacklistedWarning: String? = null
	)

	@Serializable
	class Guild(
			// É deserializado para String pois JavaScript é burro e não funciona direito com Longs
			val name: String,
			val general: GeneralConfig,
			val selfMember: SelfMember,
			val donationKey: DonationKey? = null,
			val donationConfig: DonationConfig,
			val reactionRoleConfigs: List<ReactionOption>,
			val levelUpConfig: LevelUpConfig,
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

			val donationKeys: List<DonationKey>? = null
	)

	@Serializable
	class DonationKey(
			val id: Long,
			val value: Double,
			val user: SelfMember? = null,
			val expiresAt: Long,
			val activeIn: MiniGuild? = null
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
		var punishLogChannelId: Long? = null,
		var punishLogMessage: String? = null,
		var punishmentActions: Array<WarnAction>,
		var punishmentMessages: Array<ModerationPunishmentMessageConfig>
	)

	@Serializable
	class WarnAction(
			var warnCount: Int,
			var punishmentAction: PunishmentAction,
			var customMetadata0: String? = null
	)

	@Serializable
	class ModerationPunishmentMessageConfig(
			var action: PunishmentAction,
			var message: String
	)

	@Serializable
	enum class PunishmentAction(val canChainWithWarn: Boolean) {
		BAN(true),
		KICK(true),
		MUTE(true),
		WARN(false),
		UNBAN(false),
		UNMUTE(false)
	}

	@Serializable
	class WelcomerConfig(
			val enabled: Boolean = true,
			val tellOnJoin: Boolean,
			var tellOnRemove: Boolean,
			var joinMessage: String? = null,
			var removeMessage: String? = null,
			var channelJoinId: Long? = null,
			var channelRemoveId: Long? = null,
			var tellOnPrivateJoin: Boolean = false,
			var joinPrivateMessage: String? = null,
			var tellOnBan: Boolean,
			var bannedMessage: String? = null,
			val deleteJoinMessagesAfter: Long? = null,
			val deleteRemoveMessagesAfter: Long? = null
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
			val channelToAdvertiseNewItems: Long? = null
	)

	@Serializable
	class Announcement(
			val type: String,
			val channelId: Long? = null,
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
	class TrackedYouTubeAccount(
			val channelId: Long,
			val youTubeChannelId: String,
			val message: String,
			val webhookUrl: String? = null
	)

	@Serializable
	class TrackedTwitchAccount(
			val channelId: Long,
			val twitchUserId: Long,
			val message: String,
			val webhookUrl: String? = null
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
			var giveRolesAfter: Long? = null,
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
			val id: String? = null
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
			val topic: String? = null
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