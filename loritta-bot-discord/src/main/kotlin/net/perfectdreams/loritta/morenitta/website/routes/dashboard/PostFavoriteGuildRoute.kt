package net.perfectdreams.loritta.morenitta.website.routes.dashboard

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserWebsiteSettings
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils
import net.perfectdreams.loritta.morenitta.website.utils.FavoritedGuild
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.SelectGuildProfileDashboardView.Companion.favoriteGuild
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.upsert

class PostFavoriteGuildRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard/favorite-guild") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val parameters = call.receiveParameters()
		val guildId = parameters.getOrFail("guildId").toLong()
		val favorited = parameters.getOrFail("favorited").toBoolean()

		val result = loritta.transaction {
			val websiteSettings = UserWebsiteSettings.selectAll()
				.where {
					UserWebsiteSettings.id eq userIdentification.id.toLong()
				}.firstOrNull()

			val currentlyFavoritedGuilds = websiteSettings?.get(UserWebsiteSettings.favoritedGuilds)
				?.let { Json.decodeFromString<List<FavoritedGuild>>(it) } ?: listOf()

			val newFavoritedGuilds = currentlyFavoritedGuilds.toMutableSet()

			if (favorited && newFavoritedGuilds.size >= 200)
				return@transaction Result.TooManyGuilds

			if (favorited) {
				// Only add to the favorites list if there isn't any matching guild ID
				if (!newFavoritedGuilds.any { it.guildId == guildId })
					newFavoritedGuilds.add(FavoritedGuild(guildId, kotlinx.datetime.Clock.System.now()))
			} else
				newFavoritedGuilds.removeIf { it.guildId == guildId }

			UserWebsiteSettings.upsert(UserWebsiteSettings.id) {
				it[UserWebsiteSettings.id] = userIdentification.id.toLong()
				it[UserWebsiteSettings.favoritedGuilds] = Json.encodeToString(newFavoritedGuilds)
			}

			return@transaction Result.Success
		}

		when (result) {
			Result.TooManyGuilds -> {
				call.response.header(
					"HX-Trigger",
					buildJsonObject {
						put("playSoundEffect", "config-error")
						put(
							"showSpicyToast",
							EmbeddedSpicyModalUtils.encodeURIComponent(
								Json.encodeToString(
									EmbeddedSpicyToast(EmbeddedSpicyToast.Type.WARN, i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.TooManyFavorites), i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.TooManyFavoritesDescription))
								)
							)
						)
					}.toString()
				)
				call.respondText("", status = HttpStatusCode.Forbidden)
			}
			Result.Success -> {
				if (favorited) {
					call.response.header(
						"HX-Trigger",
						buildJsonObject {
							put("playSoundEffect", "config-saved")
							put(
								"showSpicyToast",
								EmbeddedSpicyModalUtils.encodeURIComponent(
									Json.encodeToString(
										EmbeddedSpicyToast(EmbeddedSpicyToast.Type.SUCCESS, i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.ServerFavorited), i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.FavoritesDescription))
									)
								)
							)
						}.toString()
					)

					call.respondHtml(
						createHTML()
							.body {
								favoriteGuild(i18nContext, guildId, true)
							}
					)
				} else {
					call.response.header(
						"HX-Trigger",
						buildJsonObject {
							put("playSoundEffect", "config-saved")
							put(
								"showSpicyToast",
								EmbeddedSpicyModalUtils.encodeURIComponent(
									Json.encodeToString(
										EmbeddedSpicyToast(EmbeddedSpicyToast.Type.SUCCESS, i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.ServerUnfavorited), null)
									)
								)
							)
						}.toString()
					)

					call.respondText(
						createHTML()
							.body {
								favoriteGuild(i18nContext, guildId, false)
							}
					)
				}
			}
		}
	}

	sealed class Result {
		data object TooManyGuilds : Result()
		data object Success : Result()
	}
}