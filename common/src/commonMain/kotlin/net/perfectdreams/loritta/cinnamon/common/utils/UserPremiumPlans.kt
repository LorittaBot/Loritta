package net.perfectdreams.loritta.cinnamon.common.utils

interface UserPremiumPlans {
    val cost: Double
    val coinFlipRewardTax: Double

    val isCoinFlipBetRewardTaxed: Boolean
        get() = coinFlipRewardTax != 0.0

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
        override val coinFlipRewardTax = 0.05
    }

    object Essential : UserPremiumPlans {
        override val cost = 19.99
        override val coinFlipRewardTax = 0.05
    }

    object Recommended : UserPremiumPlans {
        override val cost = 39.99
        override val coinFlipRewardTax = 0.0
    }

    object Complete : UserPremiumPlans {
        override val cost = 99.99
        override val coinFlipRewardTax = 0.0
    }
}