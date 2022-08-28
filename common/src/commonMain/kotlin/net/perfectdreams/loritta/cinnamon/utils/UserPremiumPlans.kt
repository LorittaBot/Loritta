package net.perfectdreams.loritta.cinnamon.utils

interface UserPremiumPlans {
    val cost: Double
    val coinFlipRewardTax: Double
    val hasDailyInactivityTax: Boolean
    val displayAds: Boolean
    val customBackground: Boolean
    val customEmojisInAboutMe: Boolean

    val isCoinFlipBetRewardTaxed: Boolean
        get() = coinFlipRewardTax != 0.0

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
        override val coinFlipRewardTax = 0.05
        override val hasDailyInactivityTax = true
        override val displayAds = true
        override val customBackground = false
        override val customEmojisInAboutMe = false
    }

    object Essential : UserPremiumPlans {
        override val cost = 19.99
        override val coinFlipRewardTax = 0.05
        override val hasDailyInactivityTax = true
        override val displayAds = false
        override val customBackground = false
        override val customEmojisInAboutMe = false
    }

    object Recommended : UserPremiumPlans {
        override val cost = 39.99
        override val coinFlipRewardTax = 0.0
        override val hasDailyInactivityTax = true
        override val displayAds = false
        override val customBackground = true
        override val customEmojisInAboutMe = true
    }

    object Complete : UserPremiumPlans {
        override val cost = 99.99
        override val coinFlipRewardTax = 0.0
        override val hasDailyInactivityTax = false
        override val displayAds = false
        override val customBackground = true
        override val customEmojisInAboutMe = true
    }
}