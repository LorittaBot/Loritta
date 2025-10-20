package net.perfectdreams.loritta.morenitta.websitedashboard.components

import kotlinx.html.FlowContent

fun FlowContent.goBackToPreviousSectionButton(
    href: String,
    block: FlowContent.() -> (Unit),
) {
    discordButtonLink(ButtonStyle.NO_BACKGROUND_THEME_DEPENDENT_DARK_TEXT, href = href) {
        attributes["bliss-get"] = "[href]"
        attributes["bliss-swap:200"] = "#right-sidebar-contents -> #right-sidebar-contents (innerHTML)"
        attributes["bliss-push-url:200"] = "true"
        attributes["bliss-indicator"] = "#right-sidebar-wrapper"
        attributes["bliss-sync"] = "#left-sidebar"

        block()
    }
}