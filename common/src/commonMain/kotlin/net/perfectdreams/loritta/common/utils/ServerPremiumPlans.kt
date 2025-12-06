package net.perfectdreams.loritta.common.utils

interface ServerPremiumPlans {
	val cost: Double
	val maxYouTubeChannels: Int
	val maxTwitchChannels: Int
	val maxTwitterAccounts: Int
    val maxBlueskyAccounts: Int
	val maxUnauthorizedTwitchChannels: Int
	val hasCustomBadge: Boolean
	val memberCounterCount: Int
	val hasMusic: Boolean
	val doNotSendAds: Boolean
	val maxLevelUpRoles: Int
	val dailyMultiplier: Double
	val globalXpMultiplier: Double
    val showDropGuildInfoOnTransactions: Boolean

	companion object {
		fun getPlanFromValue(value: Double) = when {
			value >= 99.99 -> Complete
			value >= 39.99 -> Recommended
			value >= 19.99 -> Essential
			else           -> Free
		}
	}

	object Free : ServerPremiumPlans {
		override val cost = 0.0
		override val maxYouTubeChannels = 5
		override val maxTwitchChannels = maxYouTubeChannels
		override val maxTwitterAccounts = maxYouTubeChannels
        override val maxBlueskyAccounts = maxYouTubeChannels
        override val maxUnauthorizedTwitchChannels = 0
		override val hasCustomBadge = false
		override val memberCounterCount = 1
		override val hasMusic = false
		override val doNotSendAds = false
		override val maxLevelUpRoles = 15
		override val dailyMultiplier = 1.0
		override val globalXpMultiplier = 1.0
        override val showDropGuildInfoOnTransactions = false
    }

	object Essential : ServerPremiumPlans {
		override val cost = 19.99
		override val maxYouTubeChannels = 10
		override val maxTwitchChannels = maxYouTubeChannels
		override val maxTwitterAccounts = maxYouTubeChannels
        override val maxBlueskyAccounts = maxYouTubeChannels
		override val maxUnauthorizedTwitchChannels = 1
		override val hasCustomBadge = false
		override val memberCounterCount = 3
		override val hasMusic = true
		override val doNotSendAds = true
		override val maxLevelUpRoles = 15
		override val dailyMultiplier = 1.25
		override val globalXpMultiplier = dailyMultiplier
        override val showDropGuildInfoOnTransactions = false
	}

	object Recommended : ServerPremiumPlans {
		override val cost = 39.99
		override val maxYouTubeChannels = 25
		override val maxTwitchChannels = maxYouTubeChannels
		override val maxTwitterAccounts = maxYouTubeChannels
        override val maxBlueskyAccounts = maxYouTubeChannels
		override val maxUnauthorizedTwitchChannels = 5
		override val hasCustomBadge = true
		override val memberCounterCount = 3
		override val hasMusic = true
		override val doNotSendAds = true
		override val maxLevelUpRoles = 30
		override val dailyMultiplier = 1.5
		override val globalXpMultiplier = dailyMultiplier
        override val showDropGuildInfoOnTransactions = false
	}

	object Complete : ServerPremiumPlans {
		override val cost = 99.99
		override val maxYouTubeChannels = 100
		override val maxTwitchChannels = maxYouTubeChannels
		override val maxTwitterAccounts = maxYouTubeChannels
        override val maxBlueskyAccounts = maxYouTubeChannels
		override val maxUnauthorizedTwitchChannels = 10
		override val hasCustomBadge = true
		override val memberCounterCount = 3
		override val hasMusic = true
		override val doNotSendAds = true
		override val maxLevelUpRoles = 100
		override val dailyMultiplier = 2.0
		override val globalXpMultiplier = dailyMultiplier
        override val showDropGuildInfoOnTransactions = true
	}
}