package net.perfectdreams.loritta.morenitta.website.routes.api.v1.economy

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonParser
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import mu.KotlinLogging
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.loritta.cinnamon.pudding.utils.PaymentReason
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.LoriWebCode
import net.perfectdreams.loritta.morenitta.website.WebsiteAPIException
import net.perfectdreams.loritta.morenitta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class PostBundlesRoute(loritta: LorittaBot) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/economy/bundles/{bundleType}/{bundleId}") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		// This is a security measure, to avoid "high risk" purchases.
		// We will require that users need to verify their account + have MFA enabled.
		val refreshedUserIdentification = discordAuth.getUserIdentification()
		if (!WebsiteUtils.checkIfAccountHasMFAEnabled(loritta, refreshedUserIdentification))
			return

		val payload = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()).obj }

		val bundleId = payload["id"].long

		val bundle = loritta.newSuspendedTransaction {
			SonhosBundles.selectAll().where {
				SonhosBundles.id eq bundleId and (SonhosBundles.active eq true)
			}.firstOrNull()
		}

		val whoDonated = "${userIdentification.username}#${userIdentification.discriminator}"

		if (bundle != null) {
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
				null,
				metadata = buildJsonObject {
					put("bundleId", bundleId)
					put("bundleType", "dreams")
				}
			)

			call.respondJson(jsonObject("redirectUrl" to paymentUrl))
		} else {
			throw WebsiteAPIException(HttpStatusCode.NotFound,
					WebsiteUtils.createErrorPayload(
							loritta,
							LoriWebCode.ITEM_NOT_FOUND,
							"Bundle ID $bundleId not found or it is inactive"
					)
			)
		}
	}
}