package net.perfectdreams.loritta.common.utils

object DailyTaxThresholds {
    /**
     * Daily Tax thresholds, ordered by [DailyTaxThreshold.minimumSonhosForTrigger], descending
     */
    val THRESHOLDS = listOf(
        DailyTaxThreshold(
            3,
            100_000_000L,
            0.5
        ),
        DailyTaxThreshold(
            7,
            10_000_000L,
            0.25
        ),
        DailyTaxThreshold(
            14,
            1_000_000L,
            0.1
        ),
        DailyTaxThreshold(
            30,
            100_000L,
            0.05
        )
    ).sortedByDescending { it.minimumSonhosForTrigger }

    data class DailyTaxThreshold(
        val maxDayThreshold: Int,
        val minimumSonhosForTrigger: Long,
        val tax: Double
    )
}