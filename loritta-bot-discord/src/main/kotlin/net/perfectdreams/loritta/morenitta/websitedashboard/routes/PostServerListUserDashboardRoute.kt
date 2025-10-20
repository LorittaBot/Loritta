package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.url
import io.ktor.client.statement.bodyAsText
import io.ktor.http.userAgent
import io.ktor.server.application.ApplicationCall
import kotlinx.html.body
import kotlinx.html.div
import kotlinx.html.h1
import kotlinx.html.h2
import kotlinx.html.hr
import kotlinx.html.id
import kotlinx.html.img
import kotlinx.html.p
import kotlinx.html.stream.createHTML
import kotlinx.html.style
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserFavoritedGuilds
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.website.utils.extensions.respondHtml
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configureServerEntry
import net.perfectdreams.loritta.serializable.ColorTheme
import org.jetbrains.exposed.sql.selectAll

class PostServerListUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/guilds") {
    private val PREFIX = "https://discord.com/api/v10"
    private val TOKEN_BASE_URL = "$PREFIX/oauth2/token"
    private val USER_AGENT = "Loritta-Morenitta-Discord-Auth/1.0"
    private val USER_IDENTIFICATION_URL = "${PREFIX}/users/@me"
    private val USER_GUILDS_URL = "$USER_IDENTIFICATION_URL/guilds"

    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, theme: ColorTheme) {
        val resultAsText = website.loritta.http.get {
            url(USER_GUILDS_URL)
            userAgent(USER_AGENT)
            header("Authorization", "Bearer ${session.discordAccessToken}")
        }.bodyAsText()

        val favoritedGuilds = website.loritta.transaction {
            UserFavoritedGuilds.selectAll()
                .where {
                    UserFavoritedGuilds.userId eq session.userId
                }
                .map { it[UserFavoritedGuilds.guildId] }
                .toSet()
        }

        val userGuilds = Json.decodeFromString<List<DiscordLoginUserDashboardRoute.DiscordGuild>>(resultAsText)

        val sortedAndFilteredGuilds = userGuilds
            .filter { LorittaDashboardWebServer.canManageGuild(it) }
            .sortedWith(compareBy({ it.id !in favoritedGuilds }, { it.name }))

        call.respondHtml(
            createHTML(false)
                .body {
                    if (sortedAndFilteredGuilds.isEmpty()) {
                        div {
                            id = "no-server-found"

                            h1 {
                                +"¯\\_(ツ)_/¯"
                            }
                            h2 {
                                +i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.NoServerFound)
                            }

                            for (line in i18nContext.get(I18nKeysData.Website.Dashboard.ChooseAServer.TryLoggingIn)) {
                                p {
                                    +line
                                }
                            }
                        }
                    } else {
                        div(classes = "choose-your-server") {
                            for (guild in sortedAndFilteredGuilds) {
                                configureServerEntry(i18nContext, guild, guild.id in favoritedGuilds)
                            }
                        }

                        hr {}

                        div {
                            style = "display: flex; justify-content: center;"

                            img(src = "https://stuff.loritta.website/loritta-deitada-gabi.png") {
                                style = "max-width: 600px; width: 100%;"
                            }
                        }
                    }
                }
        )
    }
}