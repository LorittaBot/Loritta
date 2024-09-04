package net.perfectdreams.loritta.morenitta.website.routes.dashboard

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.util.*
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserFavoritedGuilds
import net.perfectdreams.loritta.common.locale.BaseLocale
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.LorittaBot
import net.perfectdreams.loritta.morenitta.website.routes.RequiresDiscordLoginLocalizedRoute
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.headerHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.EmbeddedSpicyModalUtils.respondBodyAsHXTrigger
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.website.views.dashboard.user.SelectGuildProfileDashboardView.Companion.favoriteGuild
import net.perfectdreams.loritta.serializable.EmbeddedSpicyToast
import net.perfectdreams.loritta.temmiewebsession.LorittaJsonWebSession
import net.perfectdreams.temmiediscordauth.TemmieDiscordAuth
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class PostFavoriteGuildRoute(loritta: LorittaBot) : RequiresDiscordLoginLocalizedRoute(loritta, "/dashboard/favorite-guild") {
	override suspend fun onAuthenticatedRequest(call: ApplicationCall, locale: BaseLocale, i18nContext: I18nContext, discordAuth: TemmieDiscordAuth, userIdentification: LorittaJsonWebSession.UserIdentification) {
		val parameters = call.receiveParameters()
		val guildId = parameters.getOrFail("guildId").toLong()
		val favorited = parameters.getOrFail("favorited").toBoolean()

		val result = loritta.transaction {
			val userIdLong = userIdentification.id.toLong()
			val currentlyFavoritedGuilds = UserFavoritedGuilds.selectAll()
				.where {
					UserFavoritedGuilds.userId eq userIdLong
				}
				.map { it[UserFavoritedGuilds.guildId] }

			val newFavoritedGuilds = currentlyFavoritedGuilds.toMutableSet()

			if (favorited && newFavoritedGuilds.size >= 200)
				return@transaction Result.TooManyGuilds

			if (favorited) {
				// Only add to the favorites list if there isn't any matching guild ID
				if (!newFavoritedGuilds.any { it == guildId })
					UserFavoritedGuilds.insert {
						it[UserFavoritedGuilds.userId] = userIdLong
						it[UserFavoritedGuilds.guildId] = guildId
						it[UserFavoritedGuilds.favoritedAt] = Instant.now()
					}
			} else
				UserFavoritedGuilds.deleteWhere {
					UserFavoritedGuilds.userId eq userIdLong and (UserFavoritedGuilds.guildId eq guildId)
				}

			return@transaction Result.Success
		}

		when (result) {
			Result.TooManyGuilds -> {
				call.respondBodyAsHXTrigger(status = HttpStatusCode.Forbidden) {
					playSoundEffect = "config-error"
					showSpicyToast(
						EmbeddedSpicyToast.Type.WARN,
						i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.TooManyFavorites),
						i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.TooManyFavoritesDescription)
					)
				}
			}
			Result.Success -> {
				if (favorited) {
					call.response.headerHXTrigger {
						playSoundEffect = "config-saved"
						showSpicyToast(
							EmbeddedSpicyToast.Type.SUCCESS,
							i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.ServerFavorited),
							i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.FavoritesDescription)
						)
					}

					call.respondHtml(
						createHTML()
							.body {
								favoriteGuild(i18nContext, guildId, true)
							}
					)
				} else {
					call.response.headerHXTrigger {
						playSoundEffect = "config-saved"
						showSpicyToast(
							EmbeddedSpicyToast.Type.SUCCESS,
							i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.ServerUnfavorited)
						)
					}

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