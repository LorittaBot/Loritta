package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent

/**
 * Swaps right sidebar contents and applies the loading indicator
 */
fun FlowContent.swapRightSidebarContentsAttributes() {
    attributes["bliss-get"] = "[href]"
    attributes["bliss-push-url:200"] = "true"
    attributes["bliss-swap:200"] = "#right-sidebar-contents -> #right-sidebar-contents (innerHTML)"
    attributes["bliss-indicator"] = "#right-sidebar-wrapper"
    attributes["bliss-sync"] = "#left-sidebar"
}
