package net.perfectdreams.loritta.utils

import com.mrpowergamerbr.loritta.utils.loritta

object FeatureFlags {
	val NEW_WEBSITE_PORT: Boolean
			get() = isEnabled(Names.NEW_WEBSITE_PORT)
	val MEMBER_COUNTER_UPDATE: Boolean
			get() = isEnabled(Names.MEMBER_COUNTER_UPDATE)
	val ALLOW_MORE_THAN_ONE_COUNTER_FOR_PREMIUM_USERS: Boolean
			get() = isEnabled(Names.ALLOW_MORE_THAN_ONE_COUNTER_FOR_PREMIUM_USERS)
	val BOTS_CAN_HAVE_FUN_IN_THE_RAFFLE_TOO: Boolean
			get() = isEnabled(Names.BOTS_CAN_HAVE_FUN_IN_THE_RAFFLE_TOO)
	val WRECK_THE_RAFFLE_STOP_THE_WHALES: Boolean
			get() = isEnabled(Names.WRECK_THE_RAFFLE_STOP_THE_WHALES)
	val SELECT_LOW_BETTING_USERS: Boolean
			get() = isEnabled(Names.SELECT_LOW_BETTING_USERS)
	val SELECT_USERS_WITH_LESS_MONEY: Boolean
			get() = isEnabled(Names.SELECT_USERS_WITH_LESS_MONEY)
	val ADVERTISE_SPARKLYPOWER: Boolean
			get() = isEnabled(Names.ADVERTISE_SPARKLYPOWER)
	val ADVERTISE_SPONSORS: Boolean
			get() = isEnabled(Names.ADVERTISE_SPONSORS)
	val LOG_COMMANDS: Boolean
			get() = isEnabled(Names.LOG_COMMANDS)
	val DISABLE_MUSIC_RATELIMIT: Boolean
			get() = isEnabled(Names.DISABLE_MUSIC_RATELIMIT)
	val DISABLE_TRANSLATE_RATELIMIT: Boolean
			get() = isEnabled(Names.DISABLE_TRANSLATE_RATELIMIT)
	val CHECK_IF_USER_IS_BANNED_IN_EVERY_MESSAGE: Boolean
		get() = isEnabled(Names.CHECK_IF_USER_IS_BANNED_IN_EVERY_MESSAGE)

	fun isEnabled(name: String): Boolean {
		return loritta.config.loritta.featureFlags.contains(name)
	}

	object Names {
		const val NEW_WEBSITE_PORT = "new-website-port"
		const val MEMBER_COUNTER_UPDATE = "member-counter-update"
		const val ALLOW_MORE_THAN_ONE_COUNTER_FOR_PREMIUM_USERS = "allow-more-than-one-counter-for-premium-users"
		const val BOTS_CAN_HAVE_FUN_IN_THE_RAFFLE_TOO = "bots-can-have-fun-in-the-raffle-too"
		const val WRECK_THE_RAFFLE_STOP_THE_WHALES = "wreck-the-raffle"
		const val SELECT_LOW_BETTING_USERS = "$WRECK_THE_RAFFLE_STOP_THE_WHALES-select-low-betting-users"
		const val SELECT_USERS_WITH_LESS_MONEY = "$WRECK_THE_RAFFLE_STOP_THE_WHALES-select-users-with-less-money"
		const val ADVERTISE_SPARKLYPOWER = "advertise-sparklypower"
		const val ADVERTISE_SPONSORS = "advertise-sponsors"
		const val LOG_COMMANDS = "log-commands"
		const val DISABLE_MUSIC_RATELIMIT = "disable-music-ratelimit"
		const val DISABLE_TRANSLATE_RATELIMIT = "disable-translate-ratelimit"
		const val CHECK_IF_USER_IS_BANNED_IN_EVERY_MESSAGE = "check-if-user-is-banned-in-every-message"
	}
}