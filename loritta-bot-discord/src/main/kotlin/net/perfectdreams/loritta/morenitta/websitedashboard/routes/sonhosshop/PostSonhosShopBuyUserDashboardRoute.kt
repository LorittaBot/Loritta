package net.perfectdreams.loritta.morenitta.websitedashboard.routes.sonhosshop

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import io.ktor.server.request.receiveText
import io.ktor.server.response.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.tables.WebsiteDiscountCoupons
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.common.utils.math.MathUtils
import net.perfectdreams.luna.toasts.EmbeddedToast
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.ClaimedWebsiteCoupon
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class PostSonhosShopBuyUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/sonhos-shop/buy") {
    @Serializable
    data class BuySonhosBundleRequest(
        val bundleId: Long,
        val couponCode: String? = null,
    )

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val request = Json.decodeFromString<BuySonhosBundleRequest>(call.receiveText())

        val now = Instant.now()

        val result = website.loritta.transaction {
            val bundle = SonhosBundles.selectAll()
                .where {
                    SonhosBundles.id eq request.bundleId and (SonhosBundles.active eq true)
                }.firstOrNull()

            if (bundle == null)
                return@transaction Result.UnknownBundle

            if (request.couponCode != null) {
                val couponData = WebsiteDiscountCoupons.selectAll()
                    .where {
                        WebsiteDiscountCoupons.code eq request.couponCode and (WebsiteDiscountCoupons.startsAt lessEq now and (WebsiteDiscountCoupons.endsAt greaterEq  now))
                    }
                    .firstOrNull()

                if (couponData != null) {
                    val paymentsThatUsedTheCouponCount = Payments.selectAll()
                        .where {
                            Payments.coupon eq couponData[WebsiteDiscountCoupons.id]
                        }
                        .count()

                    val claimedWebsiteCoupon = ClaimedWebsiteCoupon(
                        couponData[WebsiteDiscountCoupons.id].value,
                        couponData[WebsiteDiscountCoupons.code],
                        couponData[WebsiteDiscountCoupons.endsAt],
                        couponData[WebsiteDiscountCoupons.total],
                        couponData[WebsiteDiscountCoupons.maxUses],
                        paymentsThatUsedTheCouponCount,
                    )

                    // If the coupon has exceeded the uses, bail out
                    if (!claimedWebsiteCoupon.hasRemainingUses)
                        return@transaction Result.TooManyCouponUses

                    return@transaction Result.Success(
                        bundle,
                        claimedWebsiteCoupon
                    )
                } else {
                    return@transaction Result.UnknownCoupon
                }
            } else {
                return@transaction Result.Success(
                    bundle,
                    null
                )
            }
        }

        when (result) {
            is Result.Success -> {
                val whoDonated = session.cachedUserIdentification.username

                val grana = if (result.claimedWebsiteCoupon != null) {
                    result.bundle[SonhosBundles.price] * result.claimedWebsiteCoupon.total
                } else {
                    result.bundle[SonhosBundles.price]
                }

                val granaRounded = MathUtils.truncateToTwoDecimalPlaces(grana)

                val sonhos = result.bundle[SonhosBundles.sonhos]

                val paymentUrl = website.loritta.perfectPaymentsClient.createPayment(
                    website.loritta,
                    session.userId,
                    "$sonhos sonhos - $whoDonated (${session.userId})",
                    (granaRounded * 100).toLong(),
                    (granaRounded * 100).toLong(),
                    PaymentReason.SONHOS_BUNDLE,
                    "LORITTA-BUNDLE-%d",
                    result.claimedWebsiteCoupon?.couponId?.let { EntityID(it, WebsiteDiscountCoupons) },
                    null,
                    buildJsonObject {
                        put("bundleId", request.bundleId)
                        put("bundleType", "dreams")
                    }
                )

                call.response.header("Bliss-Redirect", paymentUrl)
                call.respondText("", status = HttpStatusCode.NoContent)
            }

            Result.UnknownBundle -> {
                call.respondHtmlFragment {
                    blissShowToast(createEmbeddedToast(EmbeddedToast.Type.WARN, "Bundle não existe!"))
                }
            }

            Result.UnknownCoupon -> {
                call.respondHtmlFragment {
                    blissShowToast(createEmbeddedToast(EmbeddedToast.Type.WARN, "Cupom não existe ou ele está expirado!"))
                }
            }

            Result.TooManyCouponUses -> {
                call.respondHtmlFragment {
                    blissShowToast(createEmbeddedToast(EmbeddedToast.Type.WARN, "O cupom chegou no limite de usos!"))
                }
            }
        }
    }

    private sealed class Result {
        data class Success(val bundle: ResultRow, val claimedWebsiteCoupon: ClaimedWebsiteCoupon?) : Result()
        data object UnknownBundle : Result()
        data object UnknownCoupon : Result()
        data object TooManyCouponUses : Result()
    }
}