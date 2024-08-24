package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.server.application.*
import io.ktor.server.response.*
import kotlinx.html.div
import kotlinx.html.id
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserLorittaAPITokens
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.utils.Base58
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedDashboardRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.LorittaUserAPIKeysView.Companion.tokenInputWrapper
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import java.time.Instant

class PostGenerateNewLorittaUserAPIKeyRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedDashboardRoute(loritta, "/dashboard/api-keys/generate") {
	override suspend fun onDashboardAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification, colorTheme: ColorTheme) {
		val requesterId = userIdentification.id.toLong()

		val apiToken = loritta.transaction {
			// Delete all tokens that are related to this user
			UserLorittaAPITokens.deleteWhere {
				UserLorittaAPITokens.tokenCreatorId eq requesterId
			}

			val randomBytes = ByteArray(32).apply {
				loritta.random.nextBytes(this)
			}
			val token = "lorixp_${Base58.encode(randomBytes)}"

			// And now we'll insert a new token!
			UserLorittaAPITokens.insert {
				it[UserLorittaAPITokens.tokenCreatorId] = requesterId
				it[UserLorittaAPITokens.tokenUserId] = requesterId
				it[UserLorittaAPITokens.token] = token
				it[UserLorittaAPITokens.generatedAt] = Instant.now()
			}

			return@transaction token
		}

		call.response.header(
			"HX-Trigger",
			buildJsonObject {
				put("playSoundEffect", "config-saved")
				put(
					"showSpicyToast",
					EmbeddedSpicyModalUtils.encodeURIComponent(
						Json.encodeToString(
							EmbeddedSpicyToast(
								EmbeddedSpicyToast.Type.SUCCESS,
								i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.Toast.TokenRegeneratedTitle),
								i18nContext.get(I18nKeysData.Website.Dashboard.ApiKeys.Toast.TokenRegeneratedDescription)
							)
						)
					)
				)
			}.toString()
		)

		call.respondHtml(
			createHTML()
				.div {
					id = "user-api-key-wrapper"
					style = "display: flex; gap: 0.5em;"

					tokenInputWrapper(i18nContext, apiToken)
				}
		)
	}
}