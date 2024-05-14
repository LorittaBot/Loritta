package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.datetime.Instant
import kotlinx.html.ButtonType
import kotlinx.html.button
import kotlinx.html.div
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.closeModalOnClick
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.openEmbeddedModalOnClick
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal
import net.perfectdreams.loritta.serializable.ShipEffect
import net.perfectdreams.loritta.serializable.UserId
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.selectAll
import kotlin.collections.set

class PostPreBuyShipEffectRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard/ship-effects/pre-buy") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val params = call.receiveParameters()

		val receivingEffectUserId = params.getOrFail("receivingEffectUserId").toLong()
		val shipPercentage = params.getOrFail("shipPercentage").toInt()
		val profile = loritta.getLorittaProfile(userIdentification.id.toLong())

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

		// Does the user already have an active ship effect for the same user + percentage?
		val showWarningModal = activeShipEffects.any { it.user2.value.toLong() == receivingEffectUserId && shipPercentage == it.editedShipValue }

		val confirmPurchaseModal = EmbeddedSpicyModalUtils.createEmbeddedConfirmPurchaseModal(
			i18nContext,
			3_000,
			profile?.money ?: 0L
		) {
			attributes["hx-post"] = "/${i18nContext.get(I18nKeysData.Website.LocalePathId)}/dashboard/ship-effects/buy"
			attributes["hx-vals"] = buildJsonObject {
				put("receivingEffectUserId", receivingEffectUserId.toString())
				put("shipPercentage", shipPercentage)
			}.toString()
			attributes["hx-swap"] = "none"
		}

		call.response.header("SpicyMorenitta-Use-Response-As-HXTrigger", "true")

		if (showWarningModal) {
			call.respondJson(
				buildJsonObject {
					put(
						"showSpicyModal",
						EmbeddedSpicyModalUtils.encodeURIComponent(
							Json.encodeToString(
								EmbeddedSpicyModal(
									i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.SimilarActiveEffect.Title),
									true,
									createHTML().div {
										p {
											text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.SimilarActiveEffect.Description))
										}
									},
									listOf(
										createHTML().button(classes = "discord-button no-background-theme-dependent-dark-text") {
											closeModalOnClick()
											type = ButtonType.button
											text(i18nContext.get(I18nKeysData.Website.Dashboard.Modal.Close))
										},
										createHTML().button(classes = "discord-button primary") {
											openEmbeddedModalOnClick(confirmPurchaseModal)
											type = ButtonType.button
											text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.SimilarActiveEffect.Continue))
										}
									)
								)
							)
						)
					)
				}.toString()
			)
		} else {
			call.respondJson(
				buildJsonObject {
					put(
						"showSpicyModal",
						EmbeddedSpicyModalUtils.encodeURIComponent(Json.encodeToString(confirmPurchaseModal))
					)
				}.toString()
			)
		}
	}
}