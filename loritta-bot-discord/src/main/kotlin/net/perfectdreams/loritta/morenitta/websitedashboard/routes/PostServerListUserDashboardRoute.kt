package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.server.application.*
import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserFavoritedGuilds
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configureServerEntry
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.selectAll

class PostServerListUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/guilds") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: UserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val userGuilds = session.retrieveUserGuilds()

        val favoritedGuilds = website.loritta.transaction {
            UserFavoritedGuilds.selectAll()
                .where {
                    UserFavoritedGuilds.userId eq session.userId
                }
                .map { it[UserFavoritedGuilds.guildId] }
                .toSet()
        }

        val sortedAndFilteredGuilds = userGuilds
            .filter { LorittaDashboardWebServer.canManageGuild(it) }
            .sortedWith(compareBy({ it.id !in favoritedGuilds }, { it.name }))

        call.respondHtmlFragment {
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
    }
}