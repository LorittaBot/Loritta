package net.perfectdreams.loritta.website.routes.api.v1.user

import com.github.salomonbrys.kotson.double
import com.github.salomonbrys.kotson.jsonObject
import com.github.salomonbrys.kotson.nullLong
import com.github.salomonbrys.kotson.obj
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mrpowergamerbr.loritta.dao.DonationKey
import io.ktor.application.*
import io.ktor.request.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.utils.payments.PaymentReason
import net.perfectdreams.loritta.website.routes.api.v1.RequiresAPIDiscordLoginRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.extensions.respondJson
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth

class PostDonationPaymentRoute(loritta: LorittaDiscord) : RequiresAPIDiscordLoginRoute(loritta, "/api/v1/users/donate") {
	companion object {
		private val logger = KotlinLogging.logger {}
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		// This is a security measure, to avoid "high risk" purchases.
		// We will require that users need to verify their account + have MFA enabled.
		val refreshedUserIdentification = discordAuth.getUserIdentification()
		if (!net.perfectdreams.loritta.website.utils.WebsiteUtils.checkIfAccountHasMFAEnabled(refreshedUserIdentification))
			return

		val payload = withContext(Dispatchers.IO) { JsonParser.parseString(call.receiveText()).obj }

		val whoDonated = "${userIdentification.username}#${userIdentification.discriminator}"

		logger.info { "User $whoDonated (${userIdentification.id}) wants to buy premium!" }

		var grana = payload["money"].double
		val keyId = payload["keyId"].nullLong

		val donationKey = if (keyId != null) {
			loritta.newSuspendedTransaction {
				val key = DonationKey.findById(keyId)

				if (key?.userId == userIdentification.id.toLong())
					return@newSuspendedTransaction key
				else
					return@newSuspendedTransaction null
			}
		} else {
			null
		}

		grana = Math.max(0.99, grana)
		grana = Math.min(1000.0, grana)

		val realValue = if (donationKey != null) {
			(donationKey.value * 0.8).toFloat()
		} else {
			grana.toFloat()
		}

		val storedAmount = if (donationKey != null) {
			donationKey.value.toFloat()
		} else {
			grana.toFloat()
		}

		var discount: Double? = null
		var metadata: JsonObject? = null
		if (donationKey != null) {
			discount = 0.2
			metadata = jsonObject("renewKey" to donationKey.id.value)
		}

		val paymentUrl = loritta.perfectPaymentsClient.createPayment(
				loritta,
				userIdentification.id.toLong(),
				"Doação para a Loritta - $whoDonated",
				(realValue * 100).toLong(),
				(storedAmount * 100).toLong(),
				PaymentReason.DONATION,
				"LORITTA-PREMIUM-%d",
				discount,
				metadata
		)

		call.respondJson(jsonObject("redirectUrl" to paymentUrl))
	}
}