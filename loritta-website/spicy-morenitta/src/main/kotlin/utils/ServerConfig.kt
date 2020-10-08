package utils

class ServerConfig(
		val textChannels: Array<TextChannel>,
		val roles: Array<Role>,
		val emotes: Array<Emote>,
		var selfUser: Member,
		var guildName: String,
		var memberCount: Int
)