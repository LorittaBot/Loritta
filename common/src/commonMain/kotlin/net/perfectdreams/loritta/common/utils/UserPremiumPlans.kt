package net.perfectdreams.loritta.common.utils

interface UserPremiumPlans {
	val cost: Double
	val doNotSendAds: Boolean
	val lessCooldown: Boolean
	val loriReputationRetribution: Double
	val dailyMultiplier: Double
	val totalLoraffleReward: Double
	val totalCoinFlipReward: Double
	val thirdPartySonhosTransferTax: Double
	val customBackground: Boolean
	val coinFlipRewardTax: Double
	val hasDailyInactivityTax: Boolean
	val displayAds: Boolean
	val customEmojisInAboutMe: Boolean
	val customEmojisInEmojiFight: Boolean

	val isCoinFlipBetRewardTaxed: Boolean
		get() = coinFlipRewardTax != 0.0

	// The "1800" and "6.0" come from the daily reward code
	val maxDreamsInDaily: Int
		get() = (1800 * (6.0 + dailyMultiplier)).toInt()

	companion object {
		val plans = listOf(
			Free,
			Essential,
			Recommended,
			Complete
		)

		fun getPlanFromValue(value: Double) = when {
			value >= 99.99 -> Complete
			value >= 39.99 -> Recommended
			value >= 19.99 -> Essential
			else           -> Free
		}

		fun getPlansThatDoNotHaveDailyInactivityTax() = plans.filter {
			!it.hasDailyInactivityTax
		}
	}

	object Free : UserPremiumPlans {
		override val cost = 0.0
		override val doNotSendAds = false
		override val lessCooldown = false
		override val loriReputationRetribution = 2.5
		// O "multiplier" apenas soma o valor do multiplicador final, então pode ser 0.0
		override val dailyMultiplier = 0.0
		override val totalLoraffleReward = 0.95
		override val totalCoinFlipReward = 0.95
		override val thirdPartySonhosTransferTax = 0.10
		override val customBackground = false
		override val coinFlipRewardTax = 0.05
		override val hasDailyInactivityTax = true
		override val displayAds = true
		override val customEmojisInAboutMe = false
		override val customEmojisInEmojiFight = false
	}

	object Essential : UserPremiumPlans {
		override val cost = 19.99
		override val doNotSendAds = true
		override val lessCooldown = false
		override val loriReputationRetribution = 5.0
		override val dailyMultiplier = 1.0
		override val totalLoraffleReward = 0.95
		override val totalCoinFlipReward = 0.95
		override val thirdPartySonhosTransferTax = 0.10
		override val customBackground = false
		override val coinFlipRewardTax = 0.05
		override val hasDailyInactivityTax = true
		override val displayAds = false
		override val customEmojisInAboutMe = false
		override val customEmojisInEmojiFight = false
	}

	object Recommended : UserPremiumPlans {
		override val cost = 39.99
		override val doNotSendAds = true
		override val lessCooldown = true
		override val loriReputationRetribution = 10.0
		override val dailyMultiplier = 2.0
		override val totalLoraffleReward = 1.0
		override val totalCoinFlipReward = 1.0
		override val thirdPartySonhosTransferTax = 0.0
		override val customBackground = true
		override val coinFlipRewardTax = 0.0
		override val hasDailyInactivityTax = true
		override val displayAds = false
		override val customEmojisInAboutMe = true
		override val customEmojisInEmojiFight = true
	}

	object Complete : UserPremiumPlans {
		override val cost = 99.99
		override val doNotSendAds = true
		override val lessCooldown = true
		override val loriReputationRetribution = 20.0
		override val dailyMultiplier = 6.0 // 6.0 em vez de 5.0 para ter aquele "wow"
		override val totalLoraffleReward = 1.0
		override val totalCoinFlipReward = 1.0
		override val thirdPartySonhosTransferTax = 0.0
		override val customBackground = true
		override val coinFlipRewardTax = 0.0
		override val hasDailyInactivityTax = false
		override val displayAds = false
		override val customEmojisInAboutMe = true
		override val customEmojisInEmojiFight = true
	}
}