package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import com.github.salomonbrys.kotson.jsonObject
import io.ktor.server.application.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.ShipEffect
import net.perfectdreams.loritta.morenitta.utils.gson
import net.perfectdreams.loritta.morenitta.website.evaluate
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.WebsiteUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.legacyVariables
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.LegacyPebbleProfileDashboardRawHtmlView
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and

class ShipEffectsRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/@me/dashboard/ship-effects") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val variables = call.legacyVariables(loritta, locale)

		val userId = userIdentification.id

		val user = loritta.lorittaShards.retrieveUserById(userId)!!
		val lorittaProfile = loritta.getOrCreateLorittaProfile(userId)

		variables["profileUser"] = user
		variables["lorittaProfile"] = lorittaProfile
		variables["saveType"] = "ship_effects"
		variables["profile_json"] = gson.toJson(
				WebsiteUtils.getProfileAsJson(lorittaProfile)
		)
		val shipEffects = loritta.newSuspendedTransaction {
			ShipEffect.find {
				(ShipEffects.buyerId eq user.idLong) and
						(ShipEffects.expiresAt greaterEq System.currentTimeMillis())
			}.toMutableList()
		}

		variables["ship_effects_json"] =
				gson.toJson(
						shipEffects.map {
							jsonObject(
									"buyerId" to it.buyerId,
									"user1Id" to it.user1Id,
									"user2Id" to it.user2Id,
									"editedShipValue" to it.editedShipValue,
									"expiresAt" to it.expiresAt
							)
						}
				)

		variables["profile_json"] = gson.toJson(
				WebsiteUtils.getProfileAsJson(lorittaProfile)
		)

		call.respondHtml(
			LegacyPebbleProfileDashboardRawHtmlView(
				loritta,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				userIdentification,
				UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
				"Painel de Controle",
				evaluate("profile_dashboard_ship_effects.html", variables),
				"ship_effects"
			).generateHtml()
		)
	}
}