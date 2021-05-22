package net.perfectdreams.loritta.website.routes

import com.github.salomonbrys.kotson.jsonArray
import com.github.salomonbrys.kotson.jsonObject
import com.mrpowergamerbr.loritta.dao.DonationKey
import com.mrpowergamerbr.loritta.tables.DonationKeys
import net.perfectdreams.loritta.common.locale.BaseLocale
import io.ktor.application.ApplicationCall
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.utils.RouteKey
import net.perfectdreams.loritta.website.utils.extensions.lorittaSession
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
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
				LorittaWebsite.INSTANCE.pageProvider.render(
						RouteKey.DONATE,
						listOf(
								getPathWithoutLocale(call),
								locale,
								userIdentification,
								keys
						)
				)
		)
	}
}