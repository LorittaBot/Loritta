package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.request.receiveText
import kotlinx.html.body
import kotlinx.html.stream.createHTML
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserFavoritedGuilds
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.unfavoriteGuildButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissSoundEffect
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.time.Instant

class PostFavoriteGuildUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/favorite") {
    @Serializable
    data class FavoriteGuildRequest(
        val guildId: Long
    )

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme) {
        val request = Json.decodeFromString<FavoriteGuildRequest>(call.receiveText())

        val result = website.loritta.transaction {
            val currentlyFavoritedGuilds = UserFavoritedGuilds.selectAll()
                .where {
                    UserFavoritedGuilds.userId eq session.userId
                }
                .map { it[UserFavoritedGuilds.guildId] }

            if (currentlyFavoritedGuilds.contains(request.guildId))
                return@transaction Result.AlreadyFavorited

            if (currentlyFavoritedGuilds.size >= 200)
                return@transaction Result.TooManyFavoritedGuilds

            UserFavoritedGuilds.insert {
                it[UserFavoritedGuilds.userId] = session.userId
                it[UserFavoritedGuilds.guildId] = request.guildId
                it[UserFavoritedGuilds.favoritedAt] = Instant.now()
            }

            return@transaction Result.Success
        }

        when (result) {
            Result.Success -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            unfavoriteGuildButton(i18nContext, request.guildId)

                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.SUCCESS,
                                    i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.ServerFavorited)
                                )
                            )

                            blissSoundEffect("configSaved")
                        }
                )
            }

            Result.AlreadyFavorited -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            unfavoriteGuildButton(i18nContext, request.guildId)

                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.WARN,
                                    i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.ServerAlreadyFavorited)
                                )
                            )
                        },
                    status = HttpStatusCode.BadRequest
                )
            }

            Result.TooManyFavoritedGuilds -> {
                call.respondHtml(
                    createHTML(false)
                        .body {
                            unfavoriteGuildButton(i18nContext, request.guildId)

                            blissShowToast(
                                createEmbeddedToast(
                                    EmbeddedToast.Type.WARN,
                                    i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.TooManyFavorites)
                                ) {
                                    text(i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.TooManyFavoritesDescription))
                                }
                            )
                        },
                    status = HttpStatusCode.BadRequest
                )
            }
        }
    }

    sealed class Result {
        data object Success : Result()
        data object AlreadyFavorited : Result()
        data object TooManyFavoritedGuilds: Result()
    }
}