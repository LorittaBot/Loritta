package net.perfectdreams.loritta.utils

interface UserPremiumPlans {
	val cost: Double
	val doNotSendAds: Boolean
	val lessCooldown: Boolean
	val maxDreamsInDaily: Int
	val loriReputationRetribution: Double
	val dailyMultiplier: Double
	val totalLoraffleReward: Double
	val totalCoinFlipReward: Double
	val customBackground: Boolean

	companion object {
		fun getPlanFromValue(value: Double) = when {
			value >= 99.99 -> Complete
			value >= 39.99 -> Recommended
			value >= 19.99 -> Essential
			else           -> Free
		}
	}

	object Free : UserPremiumPlans {
		override val cost = 0.0
		override val doNotSendAds = false
		override val lessCooldown = false
		override val maxDreamsInDaily = 3600
		override val loriReputationRetribution = 2.5
		// O "multiplier" apenas soma o valor do multiplicador final, então pode ser 0.0
		override val dailyMultiplier = 0.0
		override val totalLoraffleReward = 0.95
		override val totalCoinFlipReward = 0.95
		override val customBackground = false
	}

	object Essential : UserPremiumPlans {
		override val cost = 19.99
		override val doNotSendAds = true
		override val lessCooldown = false
		override val maxDreamsInDaily = 4200
		override val loriReputationRetribution = 5.0
		override val dailyMultiplier = 1.0
		override val totalLoraffleReward = 0.95
		override val totalCoinFlipReward = 0.95
		override val customBackground = false
	}

	object Recommended : UserPremiumPlans {
		override val cost = 39.99
		override val doNotSendAds = true
		override val lessCooldown = true
		override val maxDreamsInDaily = 4800
		override val loriReputationRetribution = 10.0
		override val dailyMultiplier = 2.0
		override val totalLoraffleReward = 1.0
		override val totalCoinFlipReward = 1.0
		override val customBackground = true
	}

	object Complete : UserPremiumPlans {
		override val cost = 99.99
		override val doNotSendAds = true
		override val lessCooldown = true
		override val maxDreamsInDaily = 7000
		override val loriReputationRetribution = 20.0
		override val dailyMultiplier = 6.0 // 6.0 em vez de 5.0 para ter aquele "wow"
		override val totalLoraffleReward = 1.0
		override val totalCoinFlipReward = 1.0
		override val customBackground = true
	}
}