package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.coroutines.delay
import kotlinx.html.body
import kotlinx.html.span
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.tables.WebsiteDiscountCoupons
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.extensions.SseEvent
import net.perfectdreams.loritta.morenitta.utils.extensions.writeSseEvent
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.SonhosShopView.Companion.discountCouponUses
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class GetSonhosShopCouponUsagesRemainingSSERoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedDashboardRoute(loritta, "/dashboard/sonhos-shop/coupon/{codeId}/usages") {
	override suspend fun onDashboardAuthenticatedRequest(
		call: ApplicationCall,
		locale: BaseLocale,
		i18nContext: I18nContext,
		discordAuth: TemmieDiscordAuth,
		userIdentification: LorittaJsonWebSession.UserIdentification,
		colorTheme: ColorTheme
	) {
		val couponCode = call.parameters.getOrFail("codeId")

		// Makes SSE work behind nginx
		// https://stackoverflow.com/a/33414096/7271796
		call.response.header("X-Accel-Buffering", "no")
		call.response.cacheControl(CacheControl.NoCache(null))
		call.respondBytesWriter(contentType = ContentType.Text.EventStream) {
			while (true) {
				// TODO: This could be optimized to avoid spamming the database for each user
				val now = Instant.now()

				val claimedWebsiteCoupon = loritta.transaction {
					val couponData = WebsiteDiscountCoupons.selectAll()
						.where {
							WebsiteDiscountCoupons.code eq couponCode and (WebsiteDiscountCoupons.startsAt lessEq now and (WebsiteDiscountCoupons.endsAt greaterEq now))
						}
						.firstOrNull() ?: return@transaction null

					val paymentsThatUsedTheCouponCount = Payments.selectAll()
						.where {
							Payments.coupon eq couponData[WebsiteDiscountCoupons.id]
						}
						.count()

					ClaimedWebsiteCoupon(
						couponData[WebsiteDiscountCoupons.id].value,
						couponData[WebsiteDiscountCoupons.code],
						couponData[WebsiteDiscountCoupons.endsAt],
						couponData[WebsiteDiscountCoupons.total],
						couponData[WebsiteDiscountCoupons.maxUses],
						paymentsThatUsedTheCouponCount,
					)
				}

				if (claimedWebsiteCoupon == null) {
					// If the coupon does not exist anymore, just bail out
					writeSseEvent(
						SseEvent(
							createHTML(prettyPrint = false)
								.body {
									// Technically this could be "unknown coupon" but if the website initiated the SSE call, it PROBABLY isn't
									text(i18nContext.get(I18nKeysData.Website.Dashboard.SonhosShop.CouponCodes.ExpiredCoupon))
								}
						)
					)
					flush()
					return@respondBytesWriter
				}

				writeSseEvent(
					SseEvent(
						createHTML(prettyPrint = false)
							.body {
								span {
									discountCouponUses(i18nContext, claimedWebsiteCoupon)
								}
							}
					)
				)
				flush()

				delay(1_000)
			}
		}
	}
}