package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent
import kotlinx.html.HTML
import kotlinx.html.body
import kotlinx.html.button
import kotlinx.html.canvas
import kotlinx.html.div
import kotlinx.html.head
import kotlinx.html.id
import kotlinx.html.lang
import kotlinx.html.link
import kotlinx.html.meta
import kotlinx.html.nav
import kotlinx.html.script
import kotlinx.html.title
import kotlinx.serialization.json.Json
import net.perfectdreams.i18nhelper.core.I18nContext
import net.perfectdreams.loritta.common.utils.UserPremiumPlans
import net.perfectdreams.loritta.dashboard.BlissHex
import net.perfectdreams.loritta.dashboard.EmbeddedToast
import net.perfectdreams.loritta.i18n.I18nKeysData
import net.perfectdreams.loritta.shimeji.LorittaShimejiSettings
import net.perfectdreams.loritta.morenitta.websitedashboard.LorittaDashboardWebServer
import net.perfectdreams.loritta.morenitta.websitedashboard.UserSession
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.createEmbeddedToast
import net.perfectdreams.loritta.morenitta.websitedashboard.utils.dashboardTitle
import net.perfectdreams.loritta.serializable.ColorTheme

fun HTML.dashboardBase(
    i18nContext: I18nContext,
    title: String,
    session: UserSession,
    theme: ColorTheme,
    shimejiSettings: LorittaShimejiSettings,
    userPremiumPlan: UserPremiumPlans,
    leftSidebarEntries: FlowContent.() -> Unit,
    rightSidebarContent: FlowContent.() -> Unit,
) {
    // See these HTML tags! https://blog.jim-nielsen.com/2025/dont-forget-these-html-tags/
    lang = i18nContext.get(I18nKeysData.Website.Bcp47Locale)

    head {
        meta(charset = "utf-8")

        // Necessary for responsive design on phones: If not set, the browsers uses the "Use Desktop Design"
        meta(name = "viewport", content = "width=device-width, initial-scale=1, viewport-fit=cover")

        title(dashboardTitle(i18nContext, title))

        link(rel = "stylesheet", href = "/assets/css/style.css?v=${LorittaDashboardWebServer.assets.cssBundle.hash}", type = "text/css")

        // Plausible Analytics
        script(src = "https://web-analytics.perfectdreams.net/js/plausible.js", type = "text/javascript") {
            attributes["data-domain"] = "loritta.website"
            defer = true
        }

        // Loritta!
        script(src = "/assets/js/frontend.js?v=${LorittaDashboardWebServer.assets.jsBundle.hash}", type = "text/javascript") {
            defer = true
        }
    }

    body {
        canvas(classes = "loritta-game-canvas") {
            attributes["bliss-component"] = "loritta-shimeji"
            attributes["loritta-shimeji-settings"] = BlissHex.encodeToHexString(Json.encodeToString(shimejiSettings))
        }

        div(classes = theme.className) {
            id = "app-wrapper"

            div {
                id = "toast-list"
            }

            div {
                id = "modal-list"
            }

            script(type = "application/json") {
                id = "save-changes-warning-toast-template"
                attributes["bliss-toast"] = BlissHex.encodeToHexString(
                    Json.encodeToString(
                        createEmbeddedToast(
                            EmbeddedToast.Type.WARN,
                            "Não perca as suas configurações!"
                        ) {
                            text("Você precisa salvar ou redefinir as configurações antes de mudar para outra página!")
                        }
                    )
                )
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

                    button(classes = "hamburger-button") {
                        attributes["bliss-component"] = "sidebar-toggle"
                        svgIcon(SVGIcons.List)
                    }
                }

                rightSidebar(i18nContext, userPremiumPlan.displayAds, rightSidebarContent)
            }
        }
    }
}