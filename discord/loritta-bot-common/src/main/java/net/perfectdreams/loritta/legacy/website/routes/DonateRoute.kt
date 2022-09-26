package net.perfectdreams.loritta.legacy.website.routes

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import net.perfectdreams.loritta.legacy.dao.DonationKey
import net.perfectdreams.loritta.legacy.tables.DonationKeys
import net.perfectdreams.loritta.legacy.common.locale.BaseLocale
import io.ktor.server.application.ApplicationCall
import net.perfectdreams.loritta.legacy.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.legacy.website.LorittaWebsite
import net.perfectdreams.loritta.legacy.website.utils.extensions.lorittaSession
import net.perfectdreams.loritta.legacy.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.legacy.website.views.DonateView
import org.jetbrains.exposed.sql.and

class DonateRoute(loritta: LorittaDiscord) : LocalizedRoute(loritta, "/donate") {
	override val isMainClusterOnlyRoute = true

	override suspend fun onLocalizedRequest(call: ApplicationCall, locale: BaseLocale) {
		val userIdentification = call.lorittaSession.getUserIdentification(call)

		val keys = jsonArray()

		if (userIdentification != null) {
			val donationKeys = loritta.newSuspendedTransaction {
				// Pegar keys ativas
				DonationKey.find {
					(DonationKeys.expiresAt greaterEq System.currentTimeMillis()) and (DonationKeys.userId eq userIdentification.id.toLong())
				}.toMutableList()
			}

			for (donationKey in donationKeys) {
				keys.add(
						jsonObject(
								"id" to donationKey.id.value,
								"value" to donationKey.value,
								"expiresAt" to donationKey.expiresAt
						)
				)
			}
		}

		call.respondHtml(
			DonateView(
				locale,
				getPathWithoutLocale(call),
				userIdentification,
				keys
			).generateHtml()
		)
	}
}