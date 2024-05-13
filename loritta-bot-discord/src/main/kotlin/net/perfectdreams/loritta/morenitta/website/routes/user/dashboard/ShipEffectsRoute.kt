package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.server.application.*
import kotlinx.datetime.Instant
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.user.ShipEffectsView
import net.perfectdreams.loritta.serializable.ShipEffect
import net.perfectdreams.loritta.serializable.UserId
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll

class ShipEffectsRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/user/@me/dashboard/ship-effects") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val activeShipEffects = loritta.pudding.transaction {
			ShipEffects.selectAll()
				.where {
					ShipEffects.buyerId eq userIdentification.id.toLong() and (ShipEffects.expiresAt greater System.currentTimeMillis())
				}.map { row ->
					ShipEffect(
						row[ShipEffects.id].value,
						UserId(row[ShipEffects.buyerId].toULong()),
						UserId(row[ShipEffects.user1Id].toULong()),
						UserId(row[ShipEffects.user2Id].toULong()),
						row[ShipEffects.editedShipValue],
						Instant.fromEpochMilliseconds(row[ShipEffects.expiresAt])
					)
				}
		}

		val resolvedUsers = activeShipEffects.flatMap { listOf(it.user1, it.user2, it.buyerId) }
			.distinct()
			.mapNotNull { loritta.pudding.users.getCachedUserInfoById(it) }

		println("Resolved Users: $resolvedUsers")

		call.respondHtml(
			ShipEffectsView(
				loritta,
				i18nContext,
				locale,
				getPathWithoutLocale(call),
				loritta.getLegacyLocaleById(locale.id),
				userIdentification,
				UserPremiumPlans.getPlanFromValue(loritta.getActiveMoneyFromDonations(userIdentification.id.toLong())),
				activeShipEffects,
				resolvedUsers
			).generateHtml()
		)
	}
}