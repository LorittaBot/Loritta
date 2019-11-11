package utils

import net.perfectdreams.spicymorenitta.views.dashboard.PremiumKeyView
import userdata.AutoroleConfig
import userdata.ModerationConfig
import userdata.PartnerConfig
import userdata.WelcomerConfig

class ServerConfig(
		val commandPrefix: String,
		val serverListConfig: PartnerConfig,
		val moderationConfig: ModerationConfig,
		val autoroleConfig: AutoroleConfig,
		val textChannelConfigs: Array<TextChannelConfig>,
		val defaultTextChannelConfig: TextChannelConfig,
		val joinLeaveConfig: WelcomerConfig,
		val textChannels: Array<TextChannel>,
		val roles: Array<Role>,
		val emotes: Array<Emote>,
		var permissions: Array<String>,
		var selfUser: Member,
		var guildName: String,
		var memberCount: Int,
		var donationKey: DonationKey?
)

class DonationKey(
		val value: Double,
		val userId: String,
		val expiresAt: String
)

fun ServerConfig.getTextChannelConfig(textChannel: TextChannel): TextChannelConfig {
	return getTextChannelConfig(textChannel.id)
}

fun ServerConfig.getTextChannelConfig(id: String): TextChannelConfig {
	return textChannelConfigs.firstOrNull { it.id == id } ?: defaultTextChannelConfig
}

fun ServerConfig.hasTextChannelConfig(textChannel: TextChannel): Boolean {
	return hasTextChannelConfig(textChannel.id)
}

fun ServerConfig.hasTextChannelConfig(id: String): Boolean {
	return textChannelConfigs.firstOrNull { it.id == id } != null
}