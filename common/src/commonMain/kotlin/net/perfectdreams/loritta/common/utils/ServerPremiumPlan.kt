package net.perfectdreams.loritta.common.utils

interface ServerPremiumPlan {
	val cost: Double
	val maxYouTubeChannels: Int
	val maxTwitchChannels: Int
	val maxTwitterAccounts: Int
    val maxBlueskyAccounts: Int
	val maxUnauthorizedTwitchChannels: Int
	val hasCustomBadge: Boolean
	val memberCounterCount: Int
	val maxLevelUpRoles: Int
	val dailyMultiplier: Double
    val showDropGuildInfoOnTransactions: Boolean
    val taxFreeFridays: Boolean
    val taxFreeSaturdays: Boolean

	companion object {
		fun getPlanFromValue(value: Int) = when {
			value >= 60 -> Complete
			value >= 35 -> Basic
			else        -> Free
		}
	}

	object Free : ServerPremiumPlan {
		override val cost = 0.0
		override val maxYouTubeChannels = 5
		override val maxTwitchChannels = maxYouTubeChannels
		override val maxTwitterAccounts = maxYouTubeChannels
        override val maxBlueskyAccounts = maxYouTubeChannels
        override val maxUnauthorizedTwitchChannels = 0
		override val hasCustomBadge = false
		override val memberCounterCount = 1
		override val maxLevelUpRoles = 15
		override val dailyMultiplier = 1.0
        override val showDropGuildInfoOnTransactions = false
        override val taxFreeFridays = false
        override val taxFreeSaturdays = false
	}

	object Basic : ServerPremiumPlan {
		override val cost = 35.0
		override val maxYouTubeChannels = 25
		override val maxTwitchChannels = maxYouTubeChannels
		override val maxTwitterAccounts = maxYouTubeChannels
        override val maxBlueskyAccounts = maxYouTubeChannels
		override val maxUnauthorizedTwitchChannels = 5
		override val hasCustomBadge = true
		override val memberCounterCount = 3
		override val maxLevelUpRoles = 30
		override val dailyMultiplier = 1.5
        override val showDropGuildInfoOnTransactions = false
        override val taxFreeFridays = false
        override val taxFreeSaturdays = false
	}

	object Complete : ServerPremiumPlan {
		override val cost = 60.0
		override val maxYouTubeChannels = 100
		override val maxTwitchChannels = maxYouTubeChannels
		override val maxTwitterAccounts = maxYouTubeChannels
        override val maxBlueskyAccounts = maxYouTubeChannels
		override val maxUnauthorizedTwitchChannels = 10
		override val hasCustomBadge = true
		override val memberCounterCount = 3
		override val maxLevelUpRoles = 100
		override val dailyMultiplier = 2.0
        override val showDropGuildInfoOnTransactions = true
        override val taxFreeFridays = true
        override val taxFreeSaturdays = true
	}
}