package net.perfectdreams.loritta.website.routes.api.v1.economy

import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.long
import com.github.salomonbrys.kotson.obj
import com.github.salomonbrys.kotson.string
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.utils.WebsiteUtils
import com.mrpowergamerbr.loritta.website.LoriWebCode
import com.mrpowergamerbr.loritta.website.WebsiteAPIException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.tables.SonhosBundles
import net.perfectdreams.loritta.utils.PerfectPaymentsClient
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select

class PostBundlesRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/economy/bundles/{bundleType}/{bundleId}") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val payload = JsonParser.parseString(call.receiveText()).obj

		val gateway = payload["gateway"].string
		val bundleId = payload["id"].long

		val bundle = loritta.newSuspendedTransaction {
			SonhosBundles.select {
				SonhosBundles.id eq bundleId and (SonhosBundles.active eq true)
			}.firstOrNull()
		}

		val whoDonated = "${userIdentification.username}#${userIdentification.discriminator}"

		if (bundle != null) {
			val grana = bundle[SonhosBundles.price]
			val sonhos = bundle[SonhosBundles.sonhos]

			val paymentUrl = PerfectPaymentsClient.createPayment(
					loritta,
					userIdentification.id.toLong(),
					"$sonhos sonhos - $whoDonated",
					(grana * 100).toLong(),
					"LORITTA-BUNDLE-%d",
					null,
					jsonObject(
							"bundleId" to bundleId,
							"bundleType" to "dreams"
					)
			)

			call.respondJson(jsonObject("redirectUrl" to paymentUrl))
		} else {
			throw WebsiteAPIException(HttpStatusCode.NotFound,
					WebsiteUtils.createErrorPayload(
							LoriWebCode.ITEM_NOT_FOUND,
							"Bundle ID $bundleId not found or it is inactive"
					)
			)
		}
	}
}