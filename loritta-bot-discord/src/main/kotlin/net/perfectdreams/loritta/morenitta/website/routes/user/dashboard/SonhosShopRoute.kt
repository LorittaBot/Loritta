package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.server.application.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.SonhosBundles
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.user.SonhosShopView
import net.perfectdreams.loritta.serializable.SonhosBundle
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.selectAll

class SonhosShopRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard/sonhos-shop") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val sonhosBundles = loritta.transaction {
			SonhosBundles.selectAll()
				.where { SonhosBundles.active eq true }
				.toList()
		}

		call.respondHtml(
			SonhosShopView(
				loritta,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				userIdentification,
				UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
				sonhosBundles.map {
					SonhosBundle(
						it[SonhosBundles.id].value,
						it[SonhosBundles.active],
						it[SonhosBundles.price],
						it[SonhosBundles.sonhos],
						it[SonhosBundles.bonus]
					)
				}
			).generateHtml()
		)
	}
}