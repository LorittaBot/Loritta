package net.perfectdreams.loritta.common.utils

object DailyTaxThresholds {
    /**
     * Daily Tax thresholds, ordered by [DailyTaxThreshold.minimumSonhosForTrigger], descending
     */
    val THRESHOLDS = listOf(
        DailyTaxThreshold(
            3,
            1_000_000_000L,
            0.75
        ),
        DailyTaxThreshold(
            7,
            100_000_000L,
            0.5
        ),
        DailyTaxThreshold(
            14,
            50_000_000L,
            0.1
        ),
        DailyTaxThreshold(
            30,
            10_000_000L,
            0.05
        )
    ).sortedByDescending { it.minimumSonhosForTrigger }

    data class DailyTaxThreshold(
        val maxDayThreshold: Int,
        val minimumSonhosForTrigger: Long,
        val tax: Double
    )
}