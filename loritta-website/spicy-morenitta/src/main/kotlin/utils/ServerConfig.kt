package utils

import userdata.AutoroleConfig
import userdata.ModerationConfig
import userdata.WelcomerConfig

class ServerConfig(
		val commandPrefix: String,
		val moderationConfig: ModerationConfig,
		val autoroleConfig: AutoroleConfig,
		val joinLeaveConfig: WelcomerConfig,
		val textChannels: Array<TextChannel>,
		val roles: Array<Role>,
		val emotes: Array<Emote>,
		var permissions: Array<String>,
		var selfUser: Member,
		var guildName: String,
		var memberCount: Int
)