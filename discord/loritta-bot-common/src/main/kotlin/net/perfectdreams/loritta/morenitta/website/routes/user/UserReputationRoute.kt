package net.perfectdreams.loritta.morenitta.website.routes.user

import net.perfectdreams.loritta.morenitta.dao.Reputation
import net.perfectdreams.loritta.morenitta.tables.Reputations
import net.perfectdreams.loritta.morenitta.utils.Constants
import net.perfectdreams.loritta.morenitta.utils.lorittaShards
import io.ktor.server.application.*
import io.ktor.server.request.*
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.utils.extensions.trueIp
import net.perfectdreams.loritta.morenitta.website.views.user.UserReputationView
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.or

class UserReputationRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/{userId}/rep") {
	override suspend fun onUnauthenticatedRequest(call: ApplicationCall, locale: BaseLocale) {
		if (call.request.header("User-Agent") == Constants.DISCORD_CRAWLER_USER_AGENT) {
			createReputationPage(call, locale, null, null)
			return
		}

		super.onUnauthenticatedRequest(call, locale)
	}

	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		createReputationPage(call, locale, discordAuth, userIdentification)
	}

	suspend fun createReputationPage(call: ApplicationCall, locale: BaseLocale, discordAuth: TemmieDiscordAuth?, userIdentification: LorittaJsonWebSession.UserIdentification?) {
		loritta as LorittaBot
		val userId = call.parameters["userId"] ?: return

		val user = lorittaShards.retrieveUserById(userId)!!

		// Vamos agora pegar todas as reputações
		val reputations = loritta.newSuspendedTransaction {
			Reputation.find { Reputations.receivedById eq user.idLong }
					.orderBy(Reputations.receivedAt to SortOrder.DESC)
					.toList()
		}

		val lastReputationGiven = if (userIdentification != null) {
			loritta.newSuspendedTransaction {
				Reputation.find {
					(Reputations.givenById eq userIdentification.id.toLong()) or
							(Reputations.givenByEmail eq userIdentification.email!!) or
							(Reputations.givenByIp eq call.request.trueIp)
				}
						.orderBy(Reputations.receivedAt to SortOrder.DESC)
						.limit(1)
						.firstOrNull()
			}
		} else null

		val backgroundUrl = loritta.getUserProfileBackgroundUrl(loritta.getOrCreateLorittaProfile(userId))

		call.respondHtml(
			UserReputationView(
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