package net.perfectdreams.loritta.morenitta.websitedashboard.routes

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.*
import kotlinx.html.*
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.cinnamon.pudding.tables.UserFavoritedGuilds
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.EmbeddedModal
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.morenitta.utils.LorittaDiscordOAuth2AuthorizeScopeURL
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaUserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.UnauthorizedTokenException
import net.perfectdreams.loritta.morenitta.websitedashboard.components.ButtonStyle
import net.perfectdreams.loritta.morenitta.websitedashboard.components.configureServerEntry
import net.perfectdreams.loritta.morenitta.websitedashboard.components.discordButtonLink
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.blissShowModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedModal
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.respondHtmlFragment
import net.perfectdreams.loritta.serializable.ColorTheme
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import org.jetbrains.exposed.sql.selectAll

class PostServerListUserDashboardRoute(website: LorittaDashboardWebServer) : RequiresUserAuthDashboardLocalizedRoute(website, "/guilds") {
    override suspend fun onAuthenticatedRequest(call: ApplicationCall, i18nContext: I18nContext, session: LorittaUserSession, userPremiumPlan: UserPremiumPlans, theme: ColorTheme, shimejiSettings: LorittaShimejiSettings) {
        val userGuilds = try {
            session.retrieveUserGuilds()
        } catch (_: UnauthorizedTokenException) {
            website.revokeLorittaSessionCookie(call)

            call.respondHtmlFragment(status = HttpStatusCode.Unauthorized) {
                blissShowModal(
                    createEmbeddedModal(
                        i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedModal.Title),
                        EmbeddedModal.Size.MEDIUM,
                        false,
                        {
                            style = "text-align: center;"

                            img(src = "https://stuff.loritta.website/emotes/lori-sob.png") {
                                height = "200"
                            }

                            for (line in i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedModal.Description)) {
                                p {
                                    text(line)
                                }
                            }
                        },
                        listOf {
                            discordButtonLink(ButtonStyle.PRIMARY, href = LorittaDiscordOAuth2AuthorizeScopeURL(website.loritta, null).toString()) {
                                text(i18nContext.get(I18nKeysData.Website.Dashboard.AuthorizationFailedModal.Reauthorize))
                            }
                        }
                    )
                )
            }
            return
        }

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