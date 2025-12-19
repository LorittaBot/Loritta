package net.perfectdreams.dora.components

import kotlinx.html.*
import net.perfectdreams.dora.DoraBackend
import net.perfectdreams.dora.utils.SVGIcons
import net.perfectdreams.loritta.morenitta.websitedashboard.components.svgIcon
import net.perfectdreams.luna.components.leftSidebar
import net.perfectdreams.luna.components.rightSidebar

fun HTML.dashboardBase(
    fullTitle: String,
    leftSidebarEntries: FlowContent.() -> Unit,
    rightSidebarContent: FlowContent.() -> Unit,
) {
    head {
        meta(charset = "utf-8")

        // Necessary for responsive design on phones: If not set, the browsers uses the "Use Desktop Design"
        meta(name = "viewport", content = "width=device-width, initial-scale=1, viewport-fit=cover")

        title(fullTitle)
        link(rel = "stylesheet", href = "/assets/css/style.css?v=${DoraBackend.assets.cssBundle.hash}", type = "text/css")

        script(src = "/assets/js/frontend.js?v=${DoraBackend.assets.jsBundle.hash}") {
            defer = true
        }
    }

    body {
        // We don't support other themes yet :(
        div(classes = "light-theme") {
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
                }

                nav {
                    id = "mobile-left-sidebar"

                    button(classes = "hamburger-button") {
                        attributes["bliss-component"] = "sidebar-toggle"
                        svgIcon(SVGIcons.List)
                    }

                    div {
                        id = "mobile-left-sidebar-title"
                        text(fullTitle)
                    }

                    div {
                        id = "mobile-left-sidebar-reserved-space"
                    }
                }

                rightSidebar(rightSidebarContent)
            }
        }
    }
}