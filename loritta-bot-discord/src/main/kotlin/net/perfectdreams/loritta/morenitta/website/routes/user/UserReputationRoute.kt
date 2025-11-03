package net.perfectdreams.loritta.morenitta.website.routes.user

import io.ktor.server.application.*
import io.ktor.server.request.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Reputations
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.dao.Reputation
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.UserReputationView
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.or

class UserReputationRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/{userId}/rep") {
	override suspend fun onUnauthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext) {
		if (call.request.header("User-Agent") == Constants.DISCORD_CRAWLER_USER_AGENT) {
			createReputationPage(call, i18nContext, locale, null)
			return
		}

		super.onUnauthenticatedRequest(call, locale, i18nContext)
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, session: UserSession) {
		createReputationPage(call, i18nContext, locale, session)
	}

	suspend fun createReputationPage(call: ApplicationCall, i18nContext: I18nContext, locale: BaseLocale, session: UserSession?) {
		val userId = call.parameters["userId"] ?: return

		val user = loritta.lorittaShards.retrieveUserById(userId)!!

		// Vamos agora pegar todas as reputações
		val reputations = loritta.newSuspendedTransaction {
			Reputation.find { Reputations.receivedById eq user.idLong }
					.orderBy(Reputations.receivedAt to SortOrder.DESC)
					.toList()
		}

        val userIdentification = session?.getUserIdentification(loritta)

		val lastReputationGiven = if (userIdentification != null) {
			loritta.newSuspendedTransaction {
				Reputation.find {
					(Reputations.givenById eq session.userId) or
							(Reputations.givenByEmail eq userIdentification.email!!) or
							(Reputations.givenByIp eq call.request.trueIp)
				}
						.orderBy(Reputations.receivedAt to SortOrder.DESC)
						.limit(1)
						.firstOrNull()
			}
		} else null

		val backgroundUrl = loritta.profileDesignManager.getUserProfileBackgroundUrl(loritta.getOrCreateLorittaProfile(userId))

		call.respondHtml(
			UserReputationView(
				loritta,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				userIdentification,
				user,
				lastReputationGiven,
				reputations,
				call.parameters["guild"]?.toLongOrNull(),
				call.parameters["channel"]?.toLongOrNull(),
				backgroundUrl
			).generateHtml()
		)
	}
}