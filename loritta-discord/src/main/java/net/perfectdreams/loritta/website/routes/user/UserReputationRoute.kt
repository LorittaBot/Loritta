package net.perfectdreams.loritta.website.routes.user

import com.mrpowergamerbr.loritta.Loritta
import com.mrpowergamerbr.loritta.dao.Reputation
import com.mrpowergamerbr.loritta.tables.Reputations
import com.mrpowergamerbr.loritta.utils.Constants
import net.perfectdreams.loritta.common.locale.BaseLocale
import com.mrpowergamerbr.loritta.utils.lorittaShards
import io.ktor.application.*
import io.ktor.request.*
import net.perfectdreams.loritta.platform.discord.LorittaDiscord
import net.perfectdreams.loritta.website.LorittaWebsite
import net.perfectdreams.loritta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.website.session.LorittaJsonWebSession
import net.perfectdreams.loritta.website.utils.RouteKey
import net.perfectdreams.loritta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.website.utils.extensions.trueIp
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.or

class UserReputationRoute(loritta: LorittaDiscord) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/{userId}/rep") {
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
		loritta as Loritta
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
				LorittaWebsite.INSTANCE.pageProvider.render(
						RouteKey.USER_REPUTATION,
						listOf(
								getPathWithoutLocale(call),
								locale,
								userIdentification,
								user,
								lastReputationGiven,
								reputations,
								call.parameters["guild"]?.toLongOrNull(),
								call.parameters["channel"]?.toLongOrNull(),
								backgroundUrl
						)
				)
		)
	}
}