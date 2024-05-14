package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.datetime.Clock
import kotlinx.datetime.toJavaInstant
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.Profiles
import net.perfectdreams.loritta.cinnamon.pudding.tables.ShipEffects
import net.perfectdreams.loritta.cinnamon.pudding.utils.SimpleSonhosTransactionsLogUtils
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.common.utils.TransactionType
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.closeModalOnClick
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondJson
import net.perfectdreams.loritta.serializable.EmbeddedSpicyModal
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.serializable.StoredShipEffectSonhosTransaction
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.update
import kotlin.time.Duration.Companion.days

class PostBuyShipEffectRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard/ship-effects/buy") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val params = call.receiveParameters()
		val userId = userIdentification.id.toLong()

		val receivingEffectUserId = params.getOrFail("receivingEffectUserId").toLong()
		val shipPercentage = params.getOrFail("shipPercentage").toInt()

		if (shipPercentage !in 0..100) {
			call.response.header("SpicyMorenitta-Use-Response-As-HXTrigger", "true")
			call.respondJson(
				buildJsonObject {
					put("playSoundEffect", "config-error")
					put("closeSpicyModal", null)
					put(
						"showSpicyToast",
						EmbeddedSpicyModalUtils.encodeURIComponent(
							Json.encodeToString(
								EmbeddedSpicyToast(EmbeddedSpicyToast.Type.WARN, "O valor do ship precisa estar entre 0% e 100%!", null)
							)
						)
					)
				}.toString(),
				status = HttpStatusCode.Forbidden
			)
			return
		}

		val result = loritta.transaction {
			val profileMoney = loritta.getLorittaProfile(userIdentification.id.toLong())?.money ?: 0L

			if (3_000 > profileMoney)
				return@transaction Result.NotEnoughSonhos

			val now = Clock.System.now()

			val shipEffectId = ShipEffects.insertAndGetId {
				it[ShipEffects.buyerId] = userId
				it[ShipEffects.user1Id] = userId
				it[ShipEffects.user2Id] = receivingEffectUserId
				it[ShipEffects.editedShipValue] = shipPercentage
				it[ShipEffects.expiresAt] = (now + 7.days).toEpochMilliseconds()
			}

			// Cinnamon transaction log
			SimpleSonhosTransactionsLogUtils.insert(
				userId,
				now.toJavaInstant(),
				TransactionType.SHIP_EFFECT,
				3_000,
				StoredShipEffectSonhosTransaction(shipEffectId.value)
			)

			// Remove the sonhos
			Profiles.update({ Profiles.id eq userIdentification.id.toLong() }) {
				with(SqlExpressionBuilder) {
					it.update(Profiles.money, Profiles.money - 3_000)
				}
			}

			return@transaction Result.Success
		}

		call.response.header("SpicyMorenitta-Use-Response-As-HXTrigger", "true")

		when (result) {
			Result.NotEnoughSonhos -> {
				call.respondJson(
					buildJsonObject {
						put("playSoundEffect", "config-error")
						put("closeSpicyModal", null)
						put(
							"showSpicyToast",
							EmbeddedSpicyModalUtils.encodeURIComponent(
								Json.encodeToString(
									EmbeddedSpicyToast(EmbeddedSpicyToast.Type.WARN, "Você não tem sonhos suficientes para comprar este item!", null)
								)
							)
						)
					}.toString(),
					status = HttpStatusCode.PaymentRequired
				)
			}
			Result.Success -> {
				call.respondJson(
					buildJsonObject {
						put("playSoundEffect", "config-saved")
						put("refreshActiveShipEffects", null)
						put(
							"showSpicyModal",
							EmbeddedSpicyModalUtils.encodeURIComponent(
								Json.encodeToString(
									EmbeddedSpicyModal(
										i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.EffectApplied.Title),
										true,
										createHTML()
											.div {
												val randomPicture = listOf(
													"https://stuff.loritta.website/ship/pantufa.png",
													"https://stuff.loritta.website/ship/gabriela.png"
												)

												div {
													style = "text-align: center;"

													img(src = randomPicture.random()) {
														width = "300"
													}
												}

												text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.EffectApplied.Description))
											},
										listOf(
											createHTML()
												.button(classes = "discord-button no-background-theme-dependent-dark-text") {
													type = ButtonType.submit
													closeModalOnClick()
													text(i18nContext.get(I18nKeysData.Website.Dashboard.ShipEffects.EffectApplied.ThanksLoveOracle))
												}
										)
									)
								)
							)
						)
					}.toString()
				)
			}
		}
	}

	private sealed class Result {
		data object NotEnoughSonhos : Result()
		data object Success : Result()
	}
}