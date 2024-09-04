package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.respondBodyAsHXTrigger
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class PostSonhosShopRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard/sonhos-shop") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val params = call.receiveParameters()
		val bundleId = params.getOrFail("bundleId").toLong()

		val bundle = loritta.pudding.transaction {
			SonhosBundles.selectAll()
				.where {
					SonhosBundles.id eq bundleId and (SonhosBundles.active eq true)
				}.firstOrNull()
		}

		if (bundle == null) {
			call.respondBodyAsHXTrigger(HttpStatusCode.NotFound) {
				showSpicyToast(EmbeddedSpicyToast.Type.WARN, "Bundle não existe!")
			}
			return
		}

		val whoDonated = userIdentification.username

		val grana = bundle[SonhosBundles.price]
		val sonhos = bundle[SonhosBundles.sonhos]

		val paymentUrl = loritta.perfectPaymentsClient.createPayment(
			loritta,
			userIdentification.id.toLong(),
			"$sonhos sonhos - $whoDonated (${userIdentification.id})",
			(grana * 100).toLong(),
			(grana * 100).toLong(),
			PaymentReason.SONHOS_BUNDLE,
			"LORITTA-BUNDLE-%d",
			null,
			buildJsonObject {
				put("bundleId", bundleId)
				put("bundleType", "dreams")
			}
		)

		call.response.header("HX-Redirect", paymentUrl)
		call.respondText("", status = HttpStatusCode.NoContent)
	}
}