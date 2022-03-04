package net.perfectdreams.loritta.cinnamon.common.utils

object DailyTaxThresholds {
    /**
     * Daily Tax thresholds, ordered by [DailyTaxThreshold.minimumSonhosForTrigger], descending
     */
    val THRESHOLDS = listOf(
        DailyTaxThreshold(
            3L,
            100_000_000L,
            0.5
        ),
        DailyTaxThreshold(
            7L,
            10_000_000L,
            0.25
        ),
        DailyTaxThreshold(
            14L,
            1_000_000L,
            0.1
        ),
        DailyTaxThreshold(
            30L,
            100_000L,
            0.05
        )
    ).sortedByDescending { it.minimumSonhosForTrigger }

    data class DailyTaxThreshold(
        val maxDayThreshold: Long,
        val minimumSonhosForTrigger: Long,
        val tax: Double
    )
}