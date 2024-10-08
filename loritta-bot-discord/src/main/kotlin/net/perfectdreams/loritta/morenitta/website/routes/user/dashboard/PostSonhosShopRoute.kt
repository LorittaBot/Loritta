package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Payments
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.tables.WebsiteDiscountCoupons
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.math.MathUtils
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.respondBodyAsHXTrigger
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class PostSonhosShopRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard/sonhos-shop") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val params = call.receiveParameters()
		val bundleId = params.getOrFail("bundleId").toLong()
		val couponCode = params["couponCode"]
		val now = Instant.now()

		val result = loritta.pudding.transaction {
			val bundle = SonhosBundles.selectAll()
				.where {
					SonhosBundles.id eq bundleId and (SonhosBundles.active eq true)
				}.firstOrNull()

			if (bundle == null)
				return@transaction Result.UnknownBundle

			if (couponCode != null) {
				val couponData = WebsiteDiscountCoupons.selectAll()
					.where {
						WebsiteDiscountCoupons.code eq couponCode and (WebsiteDiscountCoupons.startsAt lessEq now and (WebsiteDiscountCoupons.endsAt greaterEq  now))
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
				val whoDonated = userIdentification.username

				val grana = if (result.claimedWebsiteCoupon != null) {
					result.bundle[SonhosBundles.price] * result.claimedWebsiteCoupon.total
				} else {
					result.bundle[SonhosBundles.price]
				}

				val granaRounded = MathUtils.truncateToTwoDecimalPlaces(grana)

				val sonhos = result.bundle[SonhosBundles.sonhos]

				val paymentUrl = loritta.perfectPaymentsClient.createPayment(
					loritta,
					userIdentification.id.toLong(),
					"$sonhos sonhos - $whoDonated (${userIdentification.id})",
					(granaRounded * 100).toLong(),
					(granaRounded * 100).toLong(),
					PaymentReason.SONHOS_BUNDLE,
					"LORITTA-BUNDLE-%d",
					result.claimedWebsiteCoupon?.couponId?.let { EntityID(it, WebsiteDiscountCoupons) },
					null,
					buildJsonObject {
						put("bundleId", bundleId)
						put("bundleType", "dreams")
					}
				)

				call.response.header("HX-Redirect", paymentUrl)
				call.respondText("", status = HttpStatusCode.NoContent)
			}

			Result.UnknownBundle -> {
				call.respondBodyAsHXTrigger(HttpStatusCode.BadRequest) {
					playSoundEffect = "config-error"
					showSpicyToast(EmbeddedSpicyToast.Type.WARN, "Bundle não existe!")
				}
			}

			Result.UnknownCoupon -> {
				call.respondBodyAsHXTrigger(HttpStatusCode.BadRequest) {
					playSoundEffect = "config-error"
					showSpicyToast(EmbeddedSpicyToast.Type.WARN, "Cupom não existe ou ele está expirado!")
				}
			}

			Result.TooManyCouponUses -> {
				println("Respond body as HX Trigger")
				call.respondBodyAsHXTrigger(HttpStatusCode.BadRequest) {
					playSoundEffect = "config-error"
					showSpicyToast(EmbeddedSpicyToast.Type.WARN, "O cupom chegou no limite de usos!")
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