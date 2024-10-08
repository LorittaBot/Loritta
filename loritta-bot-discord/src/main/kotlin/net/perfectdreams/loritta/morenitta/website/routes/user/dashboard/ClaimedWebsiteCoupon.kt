package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import java.time.Instant

data class ClaimedWebsiteCoupon(
    val couponId: Long,
    val code: String,
    val endsAt: Instant,
    val total: Double,
    val maxUses: Int?,
    val paymentsThatUsedTheCouponCount: Long
) {
    val discount
        get() = 1.0 - total

    val remainingUses: Long?
        get() = if (maxUses != null) (maxUses - paymentsThatUsedTheCouponCount).coerceAtLeast(0) else null

    val hasRemainingUses: Boolean
        get() = if (maxUses != null) maxUses > paymentsThatUsedTheCouponCount else true
}