package net.perfectdreams.loritta.morenitta.websitedashboard.routes.sonhosshop

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.tables.WebsiteDiscountCoupons
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.morenitta.website.routes.user.dashboard.ClaimedWebsiteCoupon
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.sonhosBundlesWithCouponInput
import net.perfectdreams.loritta.morenitta.websitedashboard.routes.RequiresUserAuthDashboardLocalizedRoute
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.SonhosBundle
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class PostSonhosShopApplyCouponUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/sonhos-shop/coupon") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme) {
        val now = Instant.now()

        val couponCode = call.parameters["couponCode"]

        if (couponCode == null) {
            val sonhosBundles = website.loritta.transaction {
                SonhosBundles.selectAll()
                    .where { SonhosBundles.active eq true }
                    .toList()
                    .map {
                        SonhosBundle(
                            it[SonhosBundles.id].value,
                            it[SonhosBundles.active],
                            it[SonhosBundles.price],
                            it[SonhosBundles.sonhos],
                            it[SonhosBundles.bonus]
                        )
                    }
            }

            call.respondHtml(
                createHTML(false)
                    .body {
                        blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Cupom removido!"))

                        sonhosBundlesWithCouponInput(i18nContext, sonhosBundles, null)
                    }
            )
            return
        }

        val result = website.loritta.transaction {
            val couponData = WebsiteDiscountCoupons.selectAll()
                .where {
                    WebsiteDiscountCoupons.code eq couponCode and (WebsiteDiscountCoupons.startsAt lessEq now and (WebsiteDiscountCoupons.endsAt greaterEq  now))
                }
                .firstOrNull()

            val sonhosBundles = SonhosBundles.selectAll()
                .where { SonhosBundles.active eq true }
                .toList()
                .map {
                    SonhosBundle(
                        it[SonhosBundles.id].value,
                        it[SonhosBundles.active],
                        it[SonhosBundles.price],
                        it[SonhosBundles.sonhos],
                        it[SonhosBundles.bonus]
                    )
                }

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
                    paymentsThatUsedTheCouponCount
                )

                if (!claimedWebsiteCoupon.hasRemainingUses)
                    return@transaction Result.TooManyCouponUses(sonhosBundles)

                return@transaction Result.Success(
                    claimedWebsiteCoupon,
                    sonhosBundles
                )
            } else {
                return@transaction Result.CouponNotFound(sonhosBundles)
            }
        }

        when (result) {
            is Result.Success -> {
                call.respondHtml(
                    createHTML()
                        .body {
                            blissShowToast(createEmbeddedToast(EmbeddedToast.Type.SUCCESS, "Cupom ativado!"))

                            sonhosBundlesWithCouponInput(i18nContext, result.sonhosBundles, result.websiteCoupon)
                        }
                )
            }
            is Result.CouponNotFound -> {
                call.respondHtml(
                    createHTML()
                        .body {
                            blissShowToast(createEmbeddedToast(EmbeddedToast.Type.WARN, "Cupom não existe ou ele expirou!"))
                        },
                    status = HttpStatusCode.NotFound
                )
            }
            is Result.TooManyCouponUses -> {
                call.respondHtml(
                    createHTML()
                        .body {
                            blissShowToast(createEmbeddedToast(EmbeddedToast.Type.WARN, "O cupom chegou no limite de usos!"))
                        },
                    status = HttpStatusCode.BadRequest
                )
            }
        }
    }

    private sealed class Result {
        data class Success(val websiteCoupon: ClaimedWebsiteCoupon, val sonhosBundles: List<SonhosBundle>) : Result()

        data class CouponNotFound(val sonhosBundles: List<SonhosBundle>) : Result()

        data class TooManyCouponUses(val sonhosBundles: List<SonhosBundle>) : Result()
    }
}