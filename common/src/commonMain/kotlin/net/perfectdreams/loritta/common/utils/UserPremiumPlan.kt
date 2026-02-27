package net.perfectdreams.loritta.common.utils

interface UserPremiumPlan {
	val cost: Double
	val loriReputationRetribution: Double
	val dailyMultiplier: Double
	val totalLoraffleReward: Double
	val totalLotteryReward: Double
	val totalCoinFlipReward: Double
	val thirdPartySonhosTransferTax: Double
	val customBackground: Boolean
	val coinFlipRewardTax: Double
		get() = 1.0 - totalCoinFlipReward
	val hasDailyInactivityTax: Boolean
	val displayAds: Boolean
	val customEmojisInAboutMe: Boolean
	val customEmojisInEmojiFight: Boolean
	val sonhosAPIAccess: Boolean

	val isCoinFlipBetRewardTaxed: Boolean
		get() = coinFlipRewardTax != 0.0

	// The "1800" and "6.0" come from the daily reward code
	val maxDreamsInDaily: Int
		get() = (1800 * (6.0 + dailyMultiplier)).toInt()

	companion object {
		val plans = listOf(
			Free,
			Basic,
			Complete
		)

		fun getPlanFromValue(value: Int) = when {
			value >= 35 -> Complete
			value >= 25 -> Basic
			else        -> Free
		}

		fun getPlansThatDoNotHaveDailyInactivityTax() = plans.filter {
			!it.hasDailyInactivityTax
		}
	}

	object Free : UserPremiumPlan {
		override val cost = 0.0
		override val loriReputationRetribution = 2.5
		// O "multiplier" apenas soma o valor do multiplicador final, então pode ser 0.0
		override val dailyMultiplier = 0.0
		override val totalLotteryReward = 0.98
		override val totalLoraffleReward = 0.975
		override val totalCoinFlipReward = 0.975
		override val thirdPartySonhosTransferTax = 0.10
		override val customBackground = false
		override val hasDailyInactivityTax = true
		override val displayAds = true
		override val customEmojisInAboutMe = false
		override val customEmojisInEmojiFight = false
		override val sonhosAPIAccess = false
	}

	object Basic : UserPremiumPlan {
		override val cost = 24.99
		override val loriReputationRetribution = 10.0
		override val dailyMultiplier = 2.0
		override val totalLoraffleReward = 0.99
		override val totalLotteryReward = 0.99
		override val totalCoinFlipReward = 0.99
		override val thirdPartySonhosTransferTax = 0.05
		override val customBackground = false
		override val hasDailyInactivityTax = true
		override val displayAds = false
		override val customEmojisInAboutMe = true
		override val customEmojisInEmojiFight = true
		override val sonhosAPIAccess = true
	}

	object Complete : UserPremiumPlan {
		override val cost = 34.99
		override val loriReputationRetribution = 20.0
		override val dailyMultiplier = 6.0 // 6.0 em vez de 5.0 para ter aquele "wow"
		override val totalLoraffleReward = 1.0
		override val totalLotteryReward = 1.0
		override val totalCoinFlipReward = 1.0
		override val thirdPartySonhosTransferTax = 0.0
		override val customBackground = true
		override val hasDailyInactivityTax = false
		override val displayAds = false
		override val customEmojisInAboutMe = true
		override val customEmojisInEmojiFight = true
		override val sonhosAPIAccess = true
	}
}