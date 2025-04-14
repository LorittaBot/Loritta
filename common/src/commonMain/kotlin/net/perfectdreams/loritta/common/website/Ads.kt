package net.perfectdreams.loritta.common.website

object Ads {
    val RIGHT_SIDEBAR_AD = AdInfo(
        "6507189059",
        "loritta-dashboard-v2-right-sidebar",
        Size(
            160,
            600
        )
    )

    val LEFT_SIDEBAR_AD = AdInfo(
        "9602473293",
        "loritta-dashboard-v2-left-sidebar",
        Size(
            320,
            50
        )
    )

    data class AdInfo(
        val googleAdSenseId: String,
        val nitroPayId: String,
        val size: Size
    )

    data class Size(
        val width: Int,
        val height: Int
    )
}