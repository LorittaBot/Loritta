package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
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
import net.perfectdreams.loritta.morenitta.websitedashboard.components.favoriteGuildButton
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissSoundEffect
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere

class PostUnfavoriteGuildUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/unfavorite") {
    @Serializable
    data class UnfavoriteGuildRequest(
        val guildId: Long
    )

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme) {
        val request = Json.decodeFromString<UnfavoriteGuildRequest>(call.receiveText())

        val count = website.loritta.transaction {
            UserFavoritedGuilds.deleteWhere {
                UserFavoritedGuilds.userId eq session.userId and (UserFavoritedGuilds.guildId eq request.guildId)
            }
        }

        if (count >= 1) {
            call.respondHtml(
                createHTML(false)
                    .body {
                        favoriteGuildButton(i18nContext, request.guildId)

                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.SUCCESS,
                                i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.ServerUnfavorited)
                            )
                        )

                        blissSoundEffect("configSaved")
                    }
            )
        } else {
            call.respondHtml(
                createHTML(false)
                    .body {
                        blissShowToast(
                            createEmbeddedToast(
                                EmbeddedToast.Type.WARN,
                                i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.FavoriteServer.Toast.ServerNotFavorited)
                            )
                        )
                    },
                status = HttpStatusCode.BadRequest
            )
        }
    }
}