package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.html.div
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.tables.WebsiteDiscountCoupons
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.SonhosShopView.Companion.sonhosBundles
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.serializable.SonhosBundle
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class PostSonhosShopApplyCouponRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedDashboardRoute(loritta, "/dashboard/sonhos-shop/coupon") {
	override suspend fun onDashboardAuthenticatedRequest(
		call: ApplicationCall,
		locale: BaseLocale,
		i18nContext: I18nContext,
		discordAuth: TemmieDiscordAuth,
		userIdentification: LorittaJsonWebSession.UserIdentification,
		colorTheme: ColorTheme
	) {
		// Check if the coupon is valid
		val couponCode = call.receiveParameters()["code"]!!.uppercase()
		val now = Instant.now()

		val result = loritta.transaction {
			val couponData = WebsiteDiscountCoupons.selectAll()
				.where {
					WebsiteDiscountCoupons.code eq couponCode and (WebsiteDiscountCoupons.startsAt lessEq now and (WebsiteDiscountCoupons.endsAt greaterEq  now))
				}
				.firstOrNull()

			val sonhosBundles = loritta.transaction {
				SonhosBundles.selectAll()
					.where { SonhosBundles.active eq true }
					.toList()
			}.map {
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
				call.response.headerHXTrigger {
					this.playSoundEffect = "config-saved"
					this.showSpicyToast(
						EmbeddedSpicyToast.Type.SUCCESS,
						"Cupom ativado!"
					)
				}

				call.respondHtml(
					createHTML()
						.div {
							sonhosBundles(i18nContext, result.sonhosBundles, result.websiteCoupon)
						}
				)
			}
			is Result.CouponNotFound -> {
				call.response.headerHXTrigger {
					this.playSoundEffect = "config-error"
					this.showSpicyToast(
						EmbeddedSpicyToast.Type.WARN,
						"Cupom não existe ou ele está expirado!"
					)
				}

				call.respondHtml(
					createHTML()
						.div {
							sonhosBundles(i18nContext, result.sonhosBundles, null)
						}
				)
			}
			is Result.TooManyCouponUses -> {
				call.response.headerHXTrigger {
					this.playSoundEffect = "config-error"
					this.showSpicyToast(
						EmbeddedSpicyToast.Type.WARN,
						"O cupom chegou no limite de usos!"
					)
				}

				call.respondHtml(
					createHTML()
						.div {
							sonhosBundles(i18nContext, result.sonhosBundles, null)
						}
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