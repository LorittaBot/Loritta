package utils

class ServerConfig(
		val commandPrefix: String,
		val textChannels: Array<TextChannel>,
		val roles: Array<Role>,
		val emotes: Array<Emote>,
		var permissions: Array<String>,
		var selfUser: Member,
		var guildName: String,
		var memberCount: Int
)