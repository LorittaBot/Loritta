package net.perfectdreams.loritta.morenitta.website.routes.user.dashboard

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserPocketLorittaSettings
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.serializable.PocketLorittaSettings
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert
import java.time.Instant

class PostSpawnPocketLorittaRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard/pocket-loritta/spawn") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val params = call.receiveParameters()
		val type = params.getOrFail("type")
		val userId = userIdentification.id.toLong()

		val result = loritta.transaction {
			val settings = UserPocketLorittaSettings.selectAll()
				.where { UserPocketLorittaSettings.id eq userId }
				.limit(1)
				.firstOrNull()

			var newLorittaCount = settings?.get(UserPocketLorittaSettings.lorittaCount) ?: 0
			var newPantufaCount = settings?.get(UserPocketLorittaSettings.pantufaCount) ?: 0
			var newGabrielaCount = settings?.get(UserPocketLorittaSettings.gabrielaCount) ?: 0

			if (type == "LORITTA")
				newLorittaCount++
			if (type == "PANTUFA")
				newPantufaCount++
			if (type == "GABRIELA")
				newGabrielaCount++

			if (newLorittaCount + newPantufaCount + newGabrielaCount > 100)
				return@transaction Result.TooManyShimejis

			UserPocketLorittaSettings.upsert(UserPocketLorittaSettings.id) {
				it[UserPocketLorittaSettings.id] = userId
				it[UserPocketLorittaSettings.lorittaCount] = newLorittaCount
				it[UserPocketLorittaSettings.pantufaCount] = newPantufaCount
				it[UserPocketLorittaSettings.gabrielaCount] = newGabrielaCount
				it[UserPocketLorittaSettings.updatedAt] = Instant.now()
			}

			return@transaction Result.Success(PocketLorittaSettings(newLorittaCount, newPantufaCount, newGabrielaCount))
		}

		when (result) {
			Result.TooManyShimejis -> {
				call.response.header(
					"HX-Trigger",
					buildJsonObject {
						put("playSoundEffect", "config-error")
						put(
							"showSpicyToast",
							EmbeddedSpicyModalUtils.encodeURIComponent(
								Json.encodeToString(
									EmbeddedSpicyToast(EmbeddedSpicyToast.Type.WARN, "Você tem pestinhas demais!", "Deixe algumas pestinhas para outras pessoas!")
								)
							)
						)
					}.toString()
				)

				call.respondText("", status = HttpStatusCode.NoContent)
			}
			is Result.Success -> {
				call.response.header(
					"HX-Trigger",
					buildJsonObject {
						put("playSoundEffect", "config-saved")
						put("pocketLorittaSettingsSync", Json.encodeToString(result.settings))
					}.toString()
				)

				call.respondText("", status = HttpStatusCode.NoContent)
			}
		}
	}

	private sealed class Result {
		data object TooManyShimejis : Result()
		data class Success(val settings: PocketLorittaSettings) : Result()
	}
}