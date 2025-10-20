package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.nav
import kotlinx.html.script
import kotlinx.html.title
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.dashboardTitle
import net.perfectdreams.loritta.serializable.ColorTheme

fun HTML.dashboardBase(
    i18nContext: I18nContext,
    title: String,
    session: UserSession,
    theme: ColorTheme,
    leftSidebarEntries: FlowContent.() -> Unit,
    rightSidebarContent: FlowContent.() -> Unit,
) {
    head {
        meta(name = "viewport", content = "width=device-width, initial-scale=1, viewport-fit=cover")

        title(dashboardTitle(i18nContext, title))

        link(rel = "stylesheet", href = "/assets/css/style.css?v=${LorittaDashboardWebServer.assets.cssBundle.hash}", type = "text/css")
    }

    body {
        /* div {
            id = "loading"

            text("Carregando... Espere um pouco!")
        } */

        canvas(classes = "loritta-game-canvas") {
            attributes["bliss-component"] = "loritta-shimeji"
        }

        div(classes = theme.className) {
            id = "app-wrapper"

            div {
                id = "toast-list"
            }

            div {
                id = "modal-list"
            }

            div {
                id = "wrapper"

                leftSidebar {
                    div(classes = "entries") {
                        leftSidebarEntries()
                    }

                    userInfoWrapper(i18nContext, session)
                }

                nav {
                    id = "mobile-left-sidebar"

                    discordButton(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_LIGHT_TEXT) {
                        attributes["bliss-component"] = "sidebar-toggle"
                        text("Barra Lateral")
                    }
                }

                rightSidebar(i18nContext, rightSidebarContent)
            }
        }

        script(src = "/assets/js/frontend.js?v=${LorittaDashboardWebServer.assets.jsBundle.hash}", type = "text/javascript") {}
    }
}