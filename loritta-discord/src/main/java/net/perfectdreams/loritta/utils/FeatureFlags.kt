package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.utils.loritta

object FeatureFlags {
	val MEMBER_COUNTER_UPDATE: Boolean
		get() = isEnabled(Names.MEMBER_COUNTER_UPDATE)
	val ALLOW_MORE_THAN_ONE_COUNTER_FOR_PREMIUM_USERS: Boolean
		get() = isEnabled(Names.ALLOW_MORE_THAN_ONE_COUNTER_FOR_PREMIUM_USERS)
	val ADVERTISE_SPARKLYPOWER: Boolean
		get() = isEnabled(Names.ADVERTISE_SPARKLYPOWER)
	val ADVERTISE_SPONSORS: Boolean
		get() = isEnabled(Names.ADVERTISE_SPONSORS)
	val DISABLE_MUSIC_RATELIMIT: Boolean
		get() = isEnabled(Names.DISABLE_MUSIC_RATELIMIT)
	val DISABLE_TRANSLATE_RATELIMIT: Boolean
		get() = isEnabled(Names.DISABLE_TRANSLATE_RATELIMIT)
	val CHECK_IF_USER_IS_BANNED_IN_EVERY_MESSAGE: Boolean
		get() = isEnabled(Names.CHECK_IF_USER_IS_BANNED_IN_EVERY_MESSAGE)
	val UPDATE_IN_GUILD_STATS_ON_GUILD_JOIN: Boolean
		get() = isEnabled(Names.UPDATE_IN_GUILD_STATS_ON_GUILD_JOIN)
	val UPDATE_IN_GUILD_STATS_ON_GUILD_QUIT: Boolean
		get() = isEnabled(Names.UPDATE_IN_GUILD_STATS_ON_GUILD_QUIT)
	val UPDATE_IN_GUILD_STATS_ON_MESSAGE_SEND: Boolean
		get() = isEnabled(Names.UPDATE_IN_GUILD_STATS_ON_MESSAGE_SEND)
	val UPDATE_IN_GUILD_STATS_ON_RANK_FAILURE: Boolean
		get() = isEnabled(Names.UPDATE_IN_GUILD_STATS_ON_RANK_FAILURE)
	val AUTO_PURGE_GUILDS: Boolean
		get() = isEnabled(Names.AUTO_PURGE_GUILDS)
	val IMPROVED_TYPING_SEND: Boolean
		get() = isEnabled(Names.IMPROVED_TYPING_SEND)

	fun isEnabled(name: String): Boolean {
		return loritta.config.loritta.featureFlags.contains(name)
	}

	object Names {
		const val MEMBER_COUNTER_UPDATE = "member-counter-update"
		const val ALLOW_MORE_THAN_ONE_COUNTER_FOR_PREMIUM_USERS = "allow-more-than-one-counter-for-premium-users"
		const val ADVERTISE_SPARKLYPOWER = "advertise-sparklypower"
		const val ADVERTISE_SPONSORS = "advertise-sponsors"
		const val DISABLE_MUSIC_RATELIMIT = "disable-music-ratelimit"
		const val DISABLE_TRANSLATE_RATELIMIT = "disable-translate-ratelimit"
		const val CHECK_IF_USER_IS_BANNED_IN_EVERY_MESSAGE = "check-if-user-is-banned-in-every-message"
		const val UPDATE_IN_GUILD_STATS_ON_GUILD_JOIN = "update-in-guild-stats-on-guild-join"
		const val UPDATE_IN_GUILD_STATS_ON_GUILD_QUIT = "update-in-guild-stats-on-guild-quit"
		const val UPDATE_IN_GUILD_STATS_ON_MESSAGE_SEND = "update-in-guild-stats-on-message-send"
		const val UPDATE_IN_GUILD_STATS_ON_RANK_FAILURE = "update-in-guild-stats-on-rank-failure"
		const val AUTO_PURGE_GUILDS = "auto-purge-guilds"
		const val IMPROVED_TYPING_SEND = "improved-typing-send"
	}
}